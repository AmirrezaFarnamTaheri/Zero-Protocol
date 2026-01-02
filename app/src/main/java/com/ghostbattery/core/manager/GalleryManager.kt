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

        try {
            // 1. Query Images
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    uris.add(
                        Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )
                    )
                }
            }

            // 2. Query Videos
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    uris.add(
                        Uri.withAppendedPath(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            return@withContext null
        }

            if (uris.isEmpty()) return@withContext null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // The underlying Binder transaction buffer has a limited size (typically 1MB).
                // A very large list of Uris can cause a TransactionTooLargeException.
                // Build the request with an adaptive batch size to avoid hard failures.
                var batchSize = uris.size.coerceAtMost(10_000)
                while (batchSize > 0) {
                    val batch = uris.take(batchSize)
                    try {
                        return@withContext MediaStore
                            .createDeleteRequest(context.contentResolver, batch)
                            .intentSender
                    } catch (_: android.os.TransactionTooLargeException) {
                        batchSize /= 2
                    }
                }
                return@withContext null
            } else {
                for (uri in uris) {
                    try {
                        context.contentResolver.delete(uri, null, null)
                } catch (_: Exception) {
                        // Ignore individual failures, try next
                    }
                }
                null
            }
        }
    }
}
