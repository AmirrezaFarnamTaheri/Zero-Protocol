package com.ghostbattery.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ghostbattery.R
import com.ghostbattery.core.manager.AppManager
import com.ghostbattery.core.manager.GalleryManager
import com.ghostbattery.core.sos.SOSBeacon
import com.ghostbattery.core.manager.DataIncinerator
import com.ghostbattery.core.manager.SelfDestruct
import com.ghostbattery.data.PrefsManager
import com.ghostbattery.utils.PermissionHelper
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PanicDashboardActivity : AppCompatActivity() {

    private lateinit var sosBeacon: SOSBeacon
    private lateinit var galleryManager: GalleryManager
    private lateinit var appManager: AppManager
    private lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panic_dashboard)

        prefsManager = PrefsManager(this)
        sosBeacon = SOSBeacon(this)
        galleryManager = GalleryManager(this)
        appManager = AppManager(this)

        setupButtons()
        verifyReadiness()
    }

    private fun verifyReadiness() {
        val isLocationPerm = PermissionHelper.hasLocationPermission(this)
        val isStoragePerm = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // Simplified for older versions
        }
        val isContactSet = prefsManager.sosNumber.isNotEmpty()

        if (!isLocationPerm || !isStoragePerm || !isContactSet) {
            // Show a warning banner
            findViewById<TextView>(R.id.tv_warning_banner).visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_warning_banner).text = "⚠️ SYSTEM NOT READY. CHECK SETTINGS."
        }
    }

    private fun setupButtons() {
        // 1. SOS Button
        findViewById<Button>(R.id.btn_sos).setOnClickListener {
            lifecycleScope.launch {
                try {
                    val number = prefsManager.sosNumber
                    val message = prefsManager.sosMessage
                    if (number.isNotEmpty()) {
                        sosBeacon.sendEmergencySignal(number, message)
                    } else {
                        Toast.makeText(this@PanicDashboardActivity, "Set SOS Number in Settings!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@PanicDashboardActivity, "SOS Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 2. Wipe Gallery Button
        findViewById<Button>(R.id.btn_wipe_gallery).setOnClickListener {
             lifecycleScope.launch(Dispatchers.IO) {
                // 1. Standard MediaStore Wipe (Fast, Standard)
                val intentSender = galleryManager.createDeleteAllRequest()
                if (intentSender != null) {
                    startIntentSenderForResult(intentSender, 1001, null, 0, 0, 0)
                } else {
                     withContext(Dispatchers.Main) {
                        Toast.makeText(this@PanicDashboardActivity, "Gallery Clean / Access Denied", Toast.LENGTH_SHORT).show()
                     }
                }

                // 2. The Incinerator (Slow, Deep, Background)
                // Runs in parallel to destroy physical files if permission exists
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                    DataIncinerator.executeTotalPurge(this@PanicDashboardActivity)
                }
            }
        }

        // 3. Uninstall Apps Button (Refined Loop)
        findViewById<Button>(R.id.btn_uninstall_apps).setOnClickListener {
            executePanicProtocol()
        }

        // 4. Settings Button
        findViewById<Button>(R.id.btn_settings)?.setOnClickListener {
             startActivity(android.content.Intent(this, com.ghostbattery.ui.secure.SettingsActivity::class.java))
        }
    }

    private fun executePanicProtocol() {
        // 1. SET FLAGS
        prefsManager.isPanicModeActive = true

        // 2. SOS (First Priority - Network)
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                if (prefsManager.sosNumber.isNotEmpty()) {
                    sosBeacon.sendEmergencySignal(prefsManager.sosNumber, prefsManager.sosMessage)
                }
            } catch (e: Exception) {
                // Log error but continue
            }
        }

        // 3. DATA INCINERATION (Background Parallel)
        lifecycleScope.launch(Dispatchers.IO) {
            // This runs on Internal Storage AND SD Card simultaneously
            DataIncinerator.executeTotalPurge(this@PanicDashboardActivity)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@PanicDashboardActivity, "Incineration Complete", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. APP UNINSTALL LOOP (Foreground Interactive)
        lifecycleScope.launch(Dispatchers.Main) {
            // Slight delay to allow SOS app to open/close
            delay(2000)

            val targets = prefsManager.targetApps
            val installedTargets = appManager.findTargetApps(targets)

            if (installedTargets.isNotEmpty()) {
                 Toast.makeText(this@PanicDashboardActivity, "Purging ${installedTargets.size} apps...", Toast.LENGTH_SHORT).show()
            } else {
                 Toast.makeText(this@PanicDashboardActivity, "No targets found installed.", Toast.LENGTH_SHORT).show()
            }

            for (pkg in installedTargets) {
                appManager.requestUninstall(pkg)
                // Wait for user/accessibility to click OK
                delay(3000)
            }

            // 5. SELF DESTRUCT (The End)
            SelfDestruct.initiate(this@PanicDashboardActivity)
        }
    }
}
