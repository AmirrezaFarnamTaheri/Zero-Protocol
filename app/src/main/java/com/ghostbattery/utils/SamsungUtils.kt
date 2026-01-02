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
        val pm = context.packageManager

        fun startSafely(intent: Intent): Boolean {
            if (intent.resolveActivity(pm) == null) return false
            if (context !is android.app.Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                false
            }
        }

        val opened = startSafely(Intent("com.samsung.android.settings.SECURITY_DASHBOARD")) ||
            startSafely(Intent(Settings.ACTION_SECURITY_SETTINGS))

        if (opened) {
            Toast.makeText(context, "Enable 'Show Lockdown Option' here", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Unable to open Security settings on this device", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Checks if we are running on a Samsung device.
     */
    fun isSamsungDevice(): Boolean {
        return android.os.Build.MANUFACTURER.contains("Samsung", ignoreCase = true)
    }
}
