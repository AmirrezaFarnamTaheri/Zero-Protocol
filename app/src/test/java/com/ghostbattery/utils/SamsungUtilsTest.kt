package com.ghostbattery.utils

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class SamsungUtilsTest {

    @Test
    fun isSamsungDevice_returnsTrue_whenManufacturerIsSamsung() {
        ReflectionHelpers.setStaticField(Build::class.java, "MANUFACTURER", "Samsung")
        assertTrue(SamsungUtils.isSamsungDevice())

        ReflectionHelpers.setStaticField(Build::class.java, "MANUFACTURER", "samsung")
        assertTrue(SamsungUtils.isSamsungDevice())
    }

    @Test
    fun isSamsungDevice_returnsFalse_whenManufacturerIsNotSamsung() {
        ReflectionHelpers.setStaticField(Build::class.java, "MANUFACTURER", "Google")
        assertFalse(SamsungUtils.isSamsungDevice())
    }
}
