package com.ghostbattery.core.manager

import android.content.Context
import android.util.Log
import com.ghostbattery.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Random

object DataIncinerator {

    suspend fun executeTotalPurge(context: Context) = withContext(Dispatchers.IO) {
        val roots = StorageScanner.getAllStorageRoots(context)
        val prefs = com.ghostbattery.data.PrefsManager(context)

        // 1. STANDARD TARGETS (On all storage roots)
        for (root in roots) {
            nukeDirectory(File(root, "DCIM"))
            nukeDirectory(File(root, "Pictures"))
            nukeDirectory(File(root, "WhatsApp/Media"))
            nukeDirectory(File(root, "Android/media/com.whatsapp/WhatsApp/Media"))
            nukeDirectory(File(root, "Download"))
            nukeDirectory(File(root, "Android/data/com.sec.android.gallery3d/files/.trash"))
        }

        // 2. CUSTOM USER TARGETS
        val customTargets = prefs.customFolders
        for (path in customTargets) {
            val customDir = File(path)
            if (customDir.exists()) {
                Log.d("Incinerator", "Nuking Custom Target: $path")
                nukeDirectory(customDir)
            }
        }
    }

    private fun nukeDirectory(dir: File) {
        if (dir.exists() && dir.isDirectory) {
            dir.walkBottomUp().forEach { file ->
                if (file.isFile) {
                    incinerate(file)
                } else if (file.isDirectory) {
                    file.delete()
                }
            }
        }
    }

    private fun incinerate(file: File) {
        if (!file.exists()) return
        try {
            if (file.canWrite()) {
                // Header Corruption
                val length = file.length().coerceAtMost(4096).toInt()
                val randomData = ByteArray(length)
                Random().nextBytes(randomData)
                FileOutputStream(file).use { it.write(randomData) }
            }
            file.delete()
        } catch (e: Exception) {
            // Priority is deletion, ignore IO errors during corruption
            file.delete()
        }
    }
}
