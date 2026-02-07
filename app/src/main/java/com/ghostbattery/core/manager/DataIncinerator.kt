package com.ghostbattery.core.manager

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.ghostbattery.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.security.SecureRandom

object DataIncinerator {

    suspend fun executeTotalPurge(context: Context) = withContext(Dispatchers.IO) {
        try {
            val roots = StorageScanner.getAllStorageRoots(context)
            val prefs = com.ghostbattery.data.PrefsManager.getInstance(context)

            // 1. STANDARD TARGETS
            val targetPaths = listOf(
                "DCIM", "Pictures", "Download", "Documents",
                "WhatsApp/Media", "Android/media/com.whatsapp/WhatsApp/Media",
                "Android/data/com.sec.android.gallery3d/files/.trash"
            )

            for (root in roots) {
                for (path in targetPaths) {
                    nukeDirectory(File(root, path))
                }
            }

            // 2. CUSTOM TARGETS
            val customTargets = prefs.customFolders
            for (path in customTargets) {
                nukeDirectory(File(path))
            }
        } catch (e: Exception) {
            Log.e("Incinerator", "Global purge error: ${e.message}")
        }
    }

    @VisibleForTesting
    internal fun nukeDirectory(dir: File) {
        if (!dir.exists()) return

        dir.walkBottomUp().forEach { file ->
            try {
                if (file.isFile) {
                    incinerate(file)
                } else if (file.isDirectory) {
                    file.delete()
                }
            } catch (_: Exception) {
                // skip this file/directory
            }
        }
    }

    private fun incinerate(file: File) {
        try {
            if (file.exists() && file.canWrite()) {
                val length = file.length()
                val overwriteLen = length.coerceAtMost(4096).toInt()

                if (overwriteLen > 0) {
                    val randomData = ByteArray(overwriteLen)
                    SecureRandom().nextBytes(randomData)

                    RandomAccessFile(file, "rws").use { raf ->
                        raf.seek(0)
                        raf.write(randomData)
                        raf.fd.sync() // Force write to disk
                    }
                }
                file.delete()
            }
        } catch (e: Exception) {
            // If corruption fails (e.g. file locked), try to delete anyway
            file.delete()
        }
    }
}
