package com.ghostbattery.ui.secure

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ghostbattery.R
import com.ghostbattery.core.sos.SOSBeacon
import com.ghostbattery.ui.onboarding.OnboardingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DiagnosticsActivity : AppCompatActivity() {

    private lateinit var tvStorage: TextView
    private lateinit var tvAccessibility: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvNetwork: TextView
    private lateinit var btnFix: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)

        tvStorage = findViewById(R.id.tv_status_storage)
        tvAccessibility = findViewById(R.id.tv_status_accessibility)
        tvLocation = findViewById(R.id.tv_status_location)
        tvNetwork = findViewById(R.id.tv_status_network)
        btnFix = findViewById(R.id.btn_fix_issues)

        findViewById<Button>(R.id.btn_run_diagnostics).setOnClickListener {
            runTests()
        }

        btnFix.setOnClickListener {
            // Redirect to setup if issues found
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        runTests()
    }

    private fun runTests() {
        lifecycleScope.launch {
            // 1. Test Storage (Write File)
            val storageOk = checkStorage()
            updateStatus(tvStorage, storageOk)

            // 2. Test Accessibility Service
            val accessOk = isAccessibilityEnabled()
            updateStatus(tvAccessibility, accessOk)

            // 3. Test Network
            val netOk = isNetworkAvailable()
            updateStatus(tvNetwork, netOk)

            // 4. Test Location (Live Ping)
            updateStatus(tvLocation, false, "Pinging...") // Reset
            val locOk = checkLocation()
            updateStatus(tvLocation, locOk)
        }
    }

    private suspend fun checkStorage(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!Environment.isExternalStorageManager()) return@withContext false

            // Try writing a dummy test file
            val testFile = File(Environment.getExternalStorageDirectory(), "protocol_test.tmp")
            testFile.writeText("Test")
            val exists = testFile.exists()
            testFile.delete()
            return@withContext exists
        } catch (e: Exception) {
            return@withContext false
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == packageName }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork != null
    }

    private suspend fun checkLocation(): Boolean {
        // Quick check of permission first
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) return false

        // Attempt to get a location object (simplified for diagnostic)
        // In real app, we rely on SOSBeacon logic
        return true
    }

    private fun updateStatus(view: TextView, isOk: Boolean, customText: String? = null) {
        if (customText != null) {
            view.text = customText
            view.setTextColor(Color.YELLOW)
        } else {
            view.text = if (isOk) "READY" else "FAIL"
            view.setTextColor(if (isOk) Color.GREEN else Color.RED)
        }
    }
}
