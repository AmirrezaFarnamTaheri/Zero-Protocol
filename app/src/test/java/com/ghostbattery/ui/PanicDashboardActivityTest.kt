package com.ghostbattery.ui

import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.ghostbattery.R
import com.ghostbattery.data.PrefsManager
import com.ghostbattery.ui.secure.SettingsActivity
import com.ghostbattery.ui.help.HelpActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PanicDashboardActivityTest {

    private lateinit var mockEnvironment: MockedStatic<Environment>

    @Before
    fun setup() {
        mockEnvironment = Mockito.mockStatic(Environment::class.java)
    }

    @After
    fun tearDown() {
        mockEnvironment.close()
    }

    @Test
    fun onCreate_withMissingStorage_showsWarning() {
        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(false)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PrefsManager.getInstance(context)
        prefs.sosNumber = "" // Ensure missing SOS number to trigger warning logic

        val scenario = ActivityScenario.launch(PanicDashboardActivity::class.java)
        scenario.onActivity { activity ->
            val warning = activity.findViewById<TextView>(R.id.tv_warning_banner)
            assertEquals(View.VISIBLE, warning.visibility)
        }
    }

    @Test
    fun settingsButton_launchesSettingsActivity() {
        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(true)

        val scenario = ActivityScenario.launch(PanicDashboardActivity::class.java)
        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.btn_settings).performClick()

            val shadowActivity = Shadows.shadowOf(activity)
            val intent = shadowActivity.nextStartedActivity

            assertNotNull(intent)
            assertEquals(SettingsActivity::class.java.name, intent.component?.className)
        }
    }

    @Test
    fun helpButton_launchesHelpActivity() {
        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(true)

        val scenario = ActivityScenario.launch(PanicDashboardActivity::class.java)
        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.btn_help).performClick()

            val shadowActivity = Shadows.shadowOf(activity)
            val intent = shadowActivity.nextStartedActivity

            assertNotNull(intent)
            assertEquals(HelpActivity::class.java.name, intent.component?.className)
        }
    }
}
