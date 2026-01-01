package com.ghostbattery.core.manager

import android.content.Context
import android.content.Intent
import android.net.Uri

class AppManager(private val context: Context) {

    fun requestUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Returns a list of installed apps matching our target list.
     */
    fun findTargetApps(targets: List<String>): List<String> {
        val installedApps = context.packageManager.getInstalledPackages(0)
            .map { it.packageName }
        return targets.filter { installedApps.contains(it) }
    }
}
