package com.ghostbattery.core.manager

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GalleryManager(private val context: Context) {

    /**
     * Queries for all images/videos and returns an IntentSender to delete them.
     * The Activity must start this intent to show the system dialog.
     */
    suspend fun createDeleteAllRequest(): IntentSender? {
        return withContext(Dispatchers.IO) {
            val uris = mutableListOf<Uri>()
            val projection = arrayOf(MediaStore.MediaColumns._ID)

            // 1. Query Images
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    uris.add(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()))
                }
            }

            // 2. Query Videos
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    uris.add(Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString()))
                }
            }

            if (uris.isEmpty()) return@withContext null

            // 3. Create Write Request (Android 11+)
            // This is safer than raw file deletion and prevents Recycle Bin issues if confirmed properly
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
            } else {
                // Legacy handling for Android 10 (API 29)
                // We cannot return an IntentSender for batch deletion in the same way on API 29 without RecoverableSecurityException loop.
                // For simplicity in this specific "Panic" context, we attempt direct delete.
                // If it fails due to scoped storage, it will throw, catching in caller.
                for (uri in uris) {
                    try {
                        context.contentResolver.delete(uri, null, null)
                    } catch (e: Exception) {
                        // Ignore individual failures, try next
                    }
                }
                null
            }
        }
    }
}
