package com.ghostbattery.core.manager

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

object StorageScanner {

    /**
     * Returns a list of all writeable storage roots (Internal + Physical SD Card).
     */
    fun getAllStorageRoots(context: Context): List<File> {
        val roots = mutableListOf<File>()

        // 1. Internal Storage (Standard /sdcard)
        roots.add(Environment.getExternalStorageDirectory())

        // 2. Physical SD Cards
        // getExternalFilesDirs returns specific app folders on all volumes.
        // We traverse up to the root of that volume to find the SD card base.
        val externalDirs = ContextCompat.getExternalFilesDirs(context, null)

        for (dir in externalDirs) {
            if (dir != null) {
                val root = getRootOf(dir)
                // Avoid adding internal storage twice (it usually shows up in this list too)
                if (root != null && !roots.contains(root) && root.canWrite()) {
                    roots.add(root)
                }
            }
        }
        return roots
    }

    private fun getRootOf(file: File): File? {
        // Crude but effective way to find the mount point (e.g., /storage/XXXX-XXXX)
        val path = file.absolutePath
        if (path.contains("/storage/emulated/0")) return Environment.getExternalStorageDirectory()

        // Typical SD card pattern: /storage/1234-ABCD/Android/data
        // We split by "/" and grab the first 3 segments to isolate the volume root
        val parts = path.split("/")
        if (parts.size >= 3 && parts[1] == "storage") {
            return File("/${parts[1]}/${parts[2]}")
        }
        return null
    }
}
