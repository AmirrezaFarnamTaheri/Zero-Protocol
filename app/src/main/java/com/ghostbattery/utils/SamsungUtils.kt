package com.ghostbattery.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

object SamsungUtils {

    /**
     * Opens the specific settings page where "Show Lockdown Option" lives.
     * On Samsung One UI, this is under "Secure Lock Settings".
     */
    fun openLockdownSettings(context: Context) {
        try {
            // Attempt to open the specific Samsung Lock Screen security page
            val intent = Intent("com.samsung.android.settings.SECURITY_DASHBOARD")
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard Security Settings
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            context.startActivity(intent)
        }
        Toast.makeText(context, "Enable 'Show Lockdown Option' here", Toast.LENGTH_LONG).show()
    }

    /**
     * Checks if we are running on a Samsung device.
     */
    fun isSamsungDevice(): Boolean {
        return android.os.Build.MANUFACTURER.contains("Samsung", ignoreCase = true)
    }
}
