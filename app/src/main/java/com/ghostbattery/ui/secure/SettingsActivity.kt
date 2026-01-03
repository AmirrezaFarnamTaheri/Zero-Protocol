package com.ghostbattery.ui.secure

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ghostbattery.R
import com.ghostbattery.data.PrefsManager
import com.ghostbattery.ui.help.HelpActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var etPanicPin: EditText
    private lateinit var etSosNumber: EditText
    private lateinit var etSosMessage: EditText
    private lateinit var etTargetApps: EditText
    private lateinit var etAllowedAccessibility: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsManager = PrefsManager(this)

        etPanicPin = findViewById(R.id.et_panic_pin)
        etSosNumber = findViewById(R.id.et_sos_number)
        etSosMessage = findViewById(R.id.et_sos_message)
        etTargetApps = findViewById(R.id.et_target_apps)
        etAllowedAccessibility = findViewById(R.id.et_allowed_accessibility)

        loadCurrentSettings()

        findViewById<Button>(R.id.btn_save_settings).setOnClickListener {
            saveSettings()
        }

        // Icon Switcher
        findViewById<Button>(R.id.btn_icon_default).setOnClickListener {
            setIcon("com.ghostbattery.AliasBattery")
        }
        findViewById<Button>(R.id.btn_icon_calc).setOnClickListener {
            setIcon("com.ghostbattery.AliasCalculator")
        }
        findViewById<Button>(R.id.btn_icon_weather).setOnClickListener {
            setIcon("com.ghostbattery.AliasWeather")
        }

        // Button: Select Apps
        findViewById<Button>(R.id.btn_select_apps).setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        // Button: Select Custom Folders
        findViewById<Button>(R.id.btn_select_folders).setOnClickListener {
            startActivity(Intent(this, FolderPickerActivity::class.java))
        }

        findViewById<Button>(R.id.btn_open_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        findViewById<Button>(R.id.btn_drill_mode).setOnClickListener {
            startActivity(Intent(this, DrillModeActivity::class.java))
        }

        findViewById<Button>(R.id.btn_diagnostics).setOnClickListener {
            startActivity(Intent(this, DiagnosticsActivity::class.java))
        }
    }

    private fun loadCurrentSettings() {
        etPanicPin.setText(prefsManager.panicPin)
        etSosNumber.setText(prefsManager.sosNumber)
        etSosMessage.setText(prefsManager.sosMessage)
        etTargetApps.setText(prefsManager.targetApps.joinToString(","))
        etAllowedAccessibility.setText(prefsManager.allowedAccessibilityPackages.joinToString(","))
    }

    private fun saveSettings() {
        prefsManager.panicPin = etPanicPin.text.toString()
        prefsManager.sosNumber = etSosNumber.text.toString()
        prefsManager.sosMessage = etSosMessage.text.toString()

        // Split comma-separated list into a list
        val appsList = etTargetApps.text.toString().split(",").map { it.trim() }
        prefsManager.targetApps = appsList

        val allowedList = etAllowedAccessibility.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        prefsManager.allowedAccessibilityPackages = allowedList

        Toast.makeText(this, "Configuration Encrypted & Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setIcon(aliasName: String) {
        val packageManager = packageManager
        val aliases = listOf(
            "com.ghostbattery.AliasBattery",
            "com.ghostbattery.AliasCalculator",
            "com.ghostbattery.AliasWeather"
        )

        aliases.forEach { alias ->
            val state = if (alias == aliasName)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED

            packageManager.setComponentEnabledSetting(
                ComponentName(this, alias),
                state,
                PackageManager.DONT_KILL_APP
            )
        }
        Toast.makeText(this, "Icon updated. It may take a moment to reflect on launcher.", Toast.LENGTH_SHORT).show()
    }
}
