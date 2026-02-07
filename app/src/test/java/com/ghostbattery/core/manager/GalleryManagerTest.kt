package com.ghostbattery.core.manager

import android.app.Application
import android.content.ContentResolver
import android.database.MatrixCursor
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GalleryManagerTest {

    private lateinit var context: Application
    private lateinit var galleryManager: GalleryManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        galleryManager = GalleryManager(context)
    }

    @Test
    fun createDeleteAllRequest_returnsNull_whenNoMediaFound() = runTest {
        // By default, ContentResolver in Robolectric is empty.
        // GalleryManager should find no images/videos and return null.
        val result = galleryManager.createDeleteAllRequest()
        assertNull(result)
    }
}
