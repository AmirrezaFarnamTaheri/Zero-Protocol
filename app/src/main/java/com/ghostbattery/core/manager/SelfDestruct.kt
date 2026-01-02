package com.ghostbattery.core.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ghostbattery.data.PrefsManager

object SelfDestruct {

    fun initiate(context: Context) {
        val prefs = PrefsManager(context).apply {
            isPanicModeActive = true
        }

        val packageUri = Uri.parse("package:${context.packageName}")
        val intent = Intent(Intent.ACTION_DELETE, packageUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

        // Fail-safe: if uninstall is canceled/blocked, disarm auto-clicker.
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                context.packageManager.getPackageInfo(context.packageName, 0)
                prefs.isPanicModeActive = false
            } catch (_: Exception) {
                // App not installed anymore => no need to reset.
            }
        }, 15_000)
    }
}
