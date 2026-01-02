package com.ghostbattery.core.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ghostbattery.data.PrefsManager

object SelfDestruct {

    fun initiate(context: Context) {
        val prefs = PrefsManager(context.applicationContext).apply {
            isPanicModeActive = true
        }

        val packageUri = Uri.parse("package:${context.packageName}")
        val intent = Intent(Intent.ACTION_DELETE, packageUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

        // Fail-safe: if uninstall is canceled/blocked, disarm auto-clicker.
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val appContext = context.applicationContext
        val startMs = android.os.SystemClock.uptimeMillis()
        val maxWaitMs = 120_000L

        val disarmRunnable = object : Runnable {
            override fun run() {
                val stillInstalled = try {
                    appContext.packageManager.getPackageInfo(appContext.packageName, 0)
                    true
                } catch (_: Exception) {
                    false
                }

                // If uninstall succeeded, stop; if not, keep panic enabled until timeout.
                if (!stillInstalled) return

                val elapsed = android.os.SystemClock.uptimeMillis() - startMs
                if (elapsed >= maxWaitMs) {
                    prefs.isPanicModeActive = false
                } else {
                    handler.postDelayed(this, 2_000)
                }
            }
        }
        handler.postDelayed(disarmRunnable, 2_000)
    }
}
