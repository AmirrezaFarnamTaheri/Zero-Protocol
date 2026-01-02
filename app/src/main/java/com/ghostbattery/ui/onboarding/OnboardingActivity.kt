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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
            showExplanation("Storage Access",
                "We need 'Manage All Files' permission to permanently incinerate data. \n\nWithout this, files are simply moved to the Recycle Bin (Trash), which is forensic suicide.",
                ::requestAllFilesAccess)
        }

        findViewById<Button>(R.id.btn_grant_location).setOnClickListener {
             showExplanation("Location Access",
                "We need accurate location to send your SOS Beacon. \n\nThis is only accessed when you trigger the Panic Protocol.",
                ::requestLocationAccess)
        }

        findViewById<Button>(R.id.btn_finish_setup).setOnClickListener {
            if (isSetupComplete()) {
                startActivity(Intent(this, BatteryActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Permissions are required for Protocol Zero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showExplanation(title: String, message: String, action: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("I Understand") { _, _ -> action() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                101
            )
        }
    }

    private fun requestMediaAccessIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val missing = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                missing += Manifest.permission.READ_MEDIA_IMAGES
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                missing += Manifest.permission.READ_MEDIA_VIDEO
            }

            if (missing.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missing.toTypedArray(), 103)
            }
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
        val hasLocation =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

        val hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        }

        return hasLocation && hasStorage
    }

    override fun onResume() {
        super.onResume()
        if (isSetupComplete()) {
            findViewById<TextView>(R.id.tv_status)?.text = "Status: READY TO DEPLOY"
            findViewById<TextView>(R.id.tv_status)?.setTextColor(android.graphics.Color.GREEN)
        }

        val hasStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        findViewById<Button>(R.id.btn_grant_files).isEnabled = !hasStorage
        findViewById<Button>(R.id.btn_grant_files).text = if (hasStorage) "STORAGE SECURED" else "GRANT STORAGE ACCESS"

        val hasLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        findViewById<Button>(R.id.btn_grant_location).isEnabled = !hasLocation
        findViewById<Button>(R.id.btn_grant_location).text = if (hasLocation) "LOCATION SECURED" else "GRANT LOCATION ACCESS"
    }
}
