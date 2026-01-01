package com.ghostbattery.core.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ghostbattery.data.PrefsManager

object SelfDestruct {

    fun initiate(context: Context) {
        // 1. Set Panic Mode to active so Accessibility Service knows to click "OK"
        PrefsManager(context).apply {
            // Ensure logic in Accessibility Service checks this flag
            isPanicModeActive = true
        }

        // 2. Launch Uninstall Intent for OWN package
        val packageUri = Uri.parse("package:${context.packageName}")
        val intent = Intent(Intent.ACTION_DELETE, packageUri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
