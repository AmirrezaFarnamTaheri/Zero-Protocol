package com.ghostbattery.ui.decoy

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ghostbattery.R
import com.ghostbattery.ui.PanicDashboardActivity
import com.ghostbattery.ui.onboarding.OnboardingActivity
import com.ghostbattery.utils.PermissionHelper

class BatteryActivity : AppCompatActivity() {

    private var tapCount = 0
    private var lastTapTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery)
        setupDecoyUI()
        setupSecretTrigger()
    }

    override fun onResume() {
        super.onResume()
        checkSecurityHealth()
    }

    private fun checkSecurityHealth() {
        // If critical permissions are missing, force the setup wizard
        if (!PermissionHelper.hasLocationPermission(this)) {
            val intent = Intent(this, OnboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupDecoyUI() {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        findViewById<TextView>(R.id.tv_battery_level).text = "$batteryLevel%"

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = registerReceiver(null, filter)

        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        if (voltage != -1) {
            findViewById<TextView>(R.id.tv_voltage).text = "${voltage / 1000.0} V"
        } else {
            findViewById<TextView>(R.id.tv_voltage).text = "4.2 V" // Fallback
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        findViewById<TextView>(R.id.tv_status).text = if(isCharging) "Charging" else "Healthy"
    }

    private fun setupSecretTrigger() {
        val triggerView = findViewById<View>(R.id.view_secret_trigger)
        triggerView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTapTime > 500) tapCount = 0
            tapCount++
            lastTapTime = currentTime
            if (tapCount == 5) {
                launchPanicProtocol()
                tapCount = 0
            }
        }
    }

    private fun launchPanicProtocol() {
        Toast.makeText(this, "System Error...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, PanicDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
