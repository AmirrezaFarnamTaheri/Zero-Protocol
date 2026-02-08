package com.ghostbattery.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PermissionHelperTest {

    @Test
    fun hasLocationPermission_returnsFalse_whenNotGranted() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowContext = Shadows.shadowOf(context as android.app.Application)
        shadowContext.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        assertFalse(PermissionHelper.hasLocationPermission(context))
    }

    @Test
    fun hasLocationPermission_returnsTrue_whenGranted() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowContext = Shadows.shadowOf(context as android.app.Application)
        shadowContext.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        assertTrue(PermissionHelper.hasLocationPermission(context))
    }

    @Test
    @Config(sdk = [33]) // Android 13
    fun getRequiredMediaPermissions_onAndroid13_returnsMediaPermissions() {
        val permissions = PermissionHelper.getRequiredMediaPermissions()
        assertTrue(permissions.contains(Manifest.permission.READ_MEDIA_IMAGES))
        assertTrue(permissions.contains(Manifest.permission.READ_MEDIA_VIDEO))
    }

    @Test
    @Config(sdk = [30]) // Android 11
    fun getRequiredMediaPermissions_onAndroid11_returnsReadExternalStorage() {
        val permissions = PermissionHelper.getRequiredMediaPermissions()
        assertTrue(permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
        assertFalse(permissions.contains(Manifest.permission.READ_MEDIA_IMAGES))
    }
}
