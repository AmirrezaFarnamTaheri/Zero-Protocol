package com.ghostbattery.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ghostbattery.R
import com.ghostbattery.ui.decoy.BatteryActivity

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        findViewById<Button>(R.id.btn_grant_files).setOnClickListener {
            requestAllFilesAccess()
        }

        findViewById<Button>(R.id.btn_grant_location).setOnClickListener {
            requestLocationAccess()
        }

        findViewById<Button>(R.id.btn_finish_setup).setOnClickListener {
            if (isSetupComplete()) {
                // Navigate to the Decoy App
                startActivity(Intent(this, BatteryActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Permissions are required for Protocol Zero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivity(intent)
            } catch (e: Exception) {
                // Fallback to generic settings if the direct link fails
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            // Android 10 and below
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                101
            )
        }
    }

    private fun requestLocationAccess() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            102
        )
    }

    private fun isSetupComplete(): Boolean {
        val hasLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        return hasLocation && hasStorage
    }

    override fun onResume() {
        super.onResume()
        // Update UI state based on permissions granted returning from Settings
        if (isSetupComplete()) {
            startActivity(Intent(this, BatteryActivity::class.java))
            finish()
            return
        }

        val hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        findViewById<Button>(R.id.btn_grant_files).isEnabled = !hasStorage

        val hasLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        findViewById<Button>(R.id.btn_grant_location).isEnabled = !hasLocation
    }
}
