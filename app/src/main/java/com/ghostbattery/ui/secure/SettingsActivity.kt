package com.ghostbattery.ui.secure

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ghostbattery.R
import com.ghostbattery.data.PrefsManager
import com.ghostbattery.ui.help.HelpActivity
import com.ghostbattery.utils.SamsungUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var etSosNumber: EditText
    private lateinit var etSosMessage: EditText
    private lateinit var etTargetApps: EditText

    override fun onResume() {
        super.onResume()
        loadCurrentSettings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsManager = PrefsManager(this)

        etSosNumber = findViewById(R.id.et_sos_number)
        etSosMessage = findViewById(R.id.et_sos_message)
        etTargetApps = findViewById(R.id.et_target_apps)

        loadCurrentSettings()

        findViewById<Button>(R.id.btn_save_settings).setOnClickListener {
            saveSettings()
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

        if (SamsungUtils.isSamsungDevice()) {
            val btnSamsung = findViewById<Button?>(R.id.btn_samsung_lockdown)
            if (btnSamsung != null) {
                btnSamsung.visibility = android.view.View.VISIBLE
                btnSamsung.setOnClickListener {
                    SamsungUtils.openLockdownSettings(this)
                }
            }
        }
    }

    private fun loadCurrentSettings() {
        etSosNumber.setText(prefsManager.sosNumber)
        etSosMessage.setText(prefsManager.sosMessage)
        etTargetApps.setText(prefsManager.targetApps.joinToString(","))
    }

    private fun saveSettings() {
        prefsManager.sosNumber = etSosNumber.text.toString()
        prefsManager.sosMessage = etSosMessage.text.toString()

        // Split comma-separated list into a list
        val appsList = etTargetApps.text.toString().split(",").map { it.trim() }
        prefsManager.targetApps = appsList

        Toast.makeText(this, "Configuration Encrypted & Saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
