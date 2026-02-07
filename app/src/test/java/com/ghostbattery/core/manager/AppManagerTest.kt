package com.ghostbattery.core.manager

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class AppManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockPackageManager: PackageManager

    private lateinit var appManager: AppManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.packageManager).thenReturn(mockPackageManager)
        appManager = AppManager(mockContext)
    }

    @Test
    fun findTargetApps_returnsOnlyInstalledApps() {
        val targets = listOf("com.app.one", "com.app.two", "com.app.missing")
        val installed = listOf(
            PackageInfo().apply { packageName = "com.app.one" },
            PackageInfo().apply { packageName = "com.app.two" },
            PackageInfo().apply { packageName = "com.other.app" }
        )

        `when`(mockPackageManager.getInstalledPackages(0)).thenReturn(installed)

        val result = appManager.findTargetApps(targets)
        assertEquals(2, result.size)
        assertEquals(listOf("com.app.one", "com.app.two"), result)
    }
}
