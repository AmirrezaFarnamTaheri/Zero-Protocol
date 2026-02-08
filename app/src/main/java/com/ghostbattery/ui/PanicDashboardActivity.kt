package com.ghostbattery.ui

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ghostbattery.R
import com.ghostbattery.core.manager.AppManager
import com.ghostbattery.core.manager.DataIncinerator
import com.ghostbattery.core.manager.GalleryManager
import com.ghostbattery.core.manager.SelfDestruct
import com.ghostbattery.core.sos.SOSBeacon
import com.ghostbattery.data.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue

class PanicDashboardActivity : AppCompatActivity() {

    private lateinit var sosBeacon: SOSBeacon
    private lateinit var galleryManager: GalleryManager
    private lateinit var appManager: AppManager
    private lateinit var prefsManager: PrefsManager

    // Queue to hold pending delete requests
    private val deletionQueue: Queue<IntentSender> = LinkedList()

    // Modern ActivityResultLauncher for handling IntentSenders
    private val deleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { _ ->
        // We don't strictly care about the result code (RESULT_OK vs RESULT_CANCELED)
        // in a panic situation. We just proceed to the next batch.
        // If the user canceled, we still try the next batch.
        launchNextDeleteRequest()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panic_dashboard)

        prefsManager = PrefsManager.getInstance(this)
        sosBeacon = SOSBeacon(this)
        galleryManager = GalleryManager(this)
        appManager = AppManager(this)

        setupButtons()
        verifyReadiness()
    }

    private fun verifyReadiness() {
        // Quick health check UI
        val hasStorage = Environment.isExternalStorageManager()

        if (!hasStorage || prefsManager.sosNumber.isEmpty()) {
            findViewById<TextView>(R.id.tv_warning_banner).apply {
                visibility = View.VISIBLE
                text = "⚠️ SYSTEM NOT READY"
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btn_sos).setOnClickListener {
            launchSOS()
        }

        findViewById<Button>(R.id.btn_wipe_gallery).setOnClickListener {
            launchWipe()
        }

        findViewById<Button>(R.id.btn_uninstall_apps).setOnClickListener {
            // Trigger the full sequence manually
            executePanicProtocol()
        }

        findViewById<Button>(R.id.btn_settings)?.setOnClickListener {
             startActivity(android.content.Intent(this, com.ghostbattery.ui.secure.SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_help)?.setOnClickListener {
            startActivity(android.content.Intent(this, com.ghostbattery.ui.help.HelpActivity::class.java))
        }
    }

    private fun launchSOS() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val number = prefsManager.sosNumber
                if (number.isNotEmpty()) {
                    sosBeacon.sendEmergencySignal(number, prefsManager.sosMessage)
                } else {
                    Toast.makeText(this@PanicDashboardActivity, "Set SOS Number!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Ignore errors in panic mode
            }
        }
    }

    private fun launchWipe() {
        lifecycleScope.launch(Dispatchers.IO) {
            val hasFullAccess = Environment.isExternalStorageManager()

            if (hasFullAccess) {
                // OPTIMIZATION: If we have full file access, skip the slow GalleryManager
                // and go straight to the Incinerator. It's faster and silent.
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PanicDashboardActivity, "Incinerating Background...", Toast.LENGTH_SHORT).show()
                }
                DataIncinerator.executeTotalPurge(applicationContext)
            } else {
                // Fallback: Use the slow system dialog method
                val intentSenders = galleryManager.createDeleteAllRequest()

                withContext(Dispatchers.Main) {
                    if (!intentSenders.isNullOrEmpty()) {
                        deletionQueue.clear()
                        deletionQueue.addAll(intentSenders)
                        launchNextDeleteRequest()
                    }
                }
            }
        }
    }

    private fun launchNextDeleteRequest() {
        val sender = deletionQueue.poll() ?: return

        try {
            val request = IntentSenderRequest.Builder(sender).build()
            deleteLauncher.launch(request)
        } catch (e: Exception) {
            // If launching fails, try the next one
            launchNextDeleteRequest()
        }
    }

    private fun executePanicProtocol() {
        // 1. ENABLE AUTOMATION
        prefsManager.isPanicModeActive = true

        // 2. SOS (Network First)
        launchSOS()

        // 3. WIPE (Background)
        launchWipe()

        // 4. UNINSTALL (Foreground)
        lifecycleScope.launch(Dispatchers.Main) {
            // Give SOS app a moment to launch/send
            delay(1500)

            val targets = prefsManager.targetApps
            val installedTargets = appManager.findTargetApps(targets)

            if (installedTargets.isNotEmpty()) {
                Toast.makeText(this@PanicDashboardActivity, "Purging Apps...", Toast.LENGTH_SHORT).show()

                for (pkg in installedTargets) {
                    appManager.requestUninstall(pkg)
                    // Dynamic delay: We rely on the Accessibility Service to click fast.
                    // We wait a bit to ensure the intent fires cleanly.
                    delay(2500)
                }
            }

            // 5. SELF DESTRUCT
            SelfDestruct.initiate(this@PanicDashboardActivity)
        }
    }
}
