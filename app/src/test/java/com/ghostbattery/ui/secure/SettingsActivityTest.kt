package com.ghostbattery.ui.secure

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.ghostbattery.R
import com.ghostbattery.data.PrefsManager
import com.ghostbattery.ui.help.HelpActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class SettingsActivityTest {

    private lateinit var mockPrefsManagerStatic: MockedStatic<PrefsManager>
    private lateinit var mockPrefsManager: PrefsManager

    @Before
    fun setup() {
        mockPrefsManager = Mockito.mock(PrefsManager::class.java)

        // Mock getInstance to return our mock
        mockPrefsManagerStatic = Mockito.mockStatic(PrefsManager::class.java)
        mockPrefsManagerStatic.`when`<PrefsManager> { PrefsManager.getInstance(any()) }
            .thenReturn(mockPrefsManager)

        // Default mock behavior for getters to avoid NPEs during onCreate
        Mockito.`when`(mockPrefsManager.panicPin).thenReturn("5555")
        Mockito.`when`(mockPrefsManager.sosNumber).thenReturn("")
        Mockito.`when`(mockPrefsManager.sosMessage).thenReturn("")
        Mockito.`when`(mockPrefsManager.targetApps).thenReturn(emptyList())
        Mockito.`when`(mockPrefsManager.allowedAccessibilityPackages).thenReturn(emptySet())
    }

    @After
    fun tearDown() {
        mockPrefsManagerStatic.close()
    }

    @Test
    fun onCreate_loadsCurrentSettings() {
        Mockito.`when`(mockPrefsManager.panicPin).thenReturn("1234")
        Mockito.`when`(mockPrefsManager.sosNumber).thenReturn("911")

        val scenario = ActivityScenario.launch(SettingsActivity::class.java)
        scenario.onActivity { activity ->
            assertEquals("1234", activity.findViewById<EditText>(R.id.et_panic_pin).text.toString())
            assertEquals("911", activity.findViewById<EditText>(R.id.et_sos_number).text.toString())
        }
    }

    @Test
    fun saveSettings_updatesPrefsManager() {
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)
        scenario.onActivity { activity ->
            activity.findViewById<EditText>(R.id.et_panic_pin).setText("9999")
            activity.findViewById<Button>(R.id.btn_save_settings).performClick()

            Mockito.verify(mockPrefsManager).panicPin = "9999"
            assertEquals("Configuration Encrypted & Saved", ShadowToast.getTextOfLatestToast())
            // Activity finishes after save
            // assertTrue(activity.isFinishing) // Not necessarily checked here but good to know
        }
    }

    @Test
    fun iconSwitcher_changesComponentState() {
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)
        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.btn_icon_calc).performClick()

            val pm = activity.packageManager
            // Verify Calculator alias enabled
            val calcState = pm.getComponentEnabledSetting(ComponentName(activity, "com.ghostbattery.AliasCalculator"))
            assertEquals(PackageManager.COMPONENT_ENABLED_STATE_ENABLED, calcState)

            // Verify Battery alias disabled
            val batState = pm.getComponentEnabledSetting(ComponentName(activity, "com.ghostbattery.AliasBattery"))
            assertEquals(PackageManager.COMPONENT_ENABLED_STATE_DISABLED, batState)
        }
    }

    @Test
    fun navigationButtons_launchCorrectActivities() {
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)
        scenario.onActivity { activity ->
            // Test Help
            activity.findViewById<Button>(R.id.btn_open_help).performClick()
            val shadowActivity = Shadows.shadowOf(activity)
            val intent = shadowActivity.nextStartedActivity
            assertEquals(HelpActivity::class.java.name, intent.component?.className)
        }
    }
}
