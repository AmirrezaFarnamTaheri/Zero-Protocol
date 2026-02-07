package com.ghostbattery.data

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.anyBoolean

class PrefsManagerTest {

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var prefsManager: PrefsManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)

        prefsManager = PrefsManager(mockSharedPreferences)
    }

    @Test
    fun getSosNumber_returnsEmptyString_whenNotSet() {
        `when`(mockSharedPreferences.getString("sos_number", "")).thenReturn("")
        assertEquals("", prefsManager.sosNumber)
    }

    @Test
    fun setSosNumber_savesToSharedPreferences() {
        prefsManager.sosNumber = "12345"
        verify(mockEditor).putString("sos_number", "12345")
        verify(mockEditor).apply()
    }

    @Test
    fun isPanicModeActive_returnsFalse_byDefault() {
        `when`(mockSharedPreferences.getBoolean("panic_mode_active", false)).thenReturn(false)
        assertEquals(false, prefsManager.isPanicModeActive)
    }

    @Test
    fun setPanicModeActive_savesToSharedPreferences() {
        prefsManager.isPanicModeActive = true
        verify(mockEditor).putBoolean("panic_mode_active", true)
        verify(mockEditor).apply()
    }
}
