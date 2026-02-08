package com.ghostbattery.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.ghostbattery.R
import com.ghostbattery.ui.decoy.BatteryActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OnboardingActivityTest {

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
    fun onCreate_withNoPermissions_buttonsEnabled() {
        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(false)

        val scenario = ActivityScenario.launch(OnboardingActivity::class.java)
        scenario.onActivity { activity ->
            val btnStorage = activity.findViewById<Button>(R.id.btn_grant_files)
            val btnLocation = activity.findViewById<Button>(R.id.btn_grant_location)

            assertTrue(btnStorage.isEnabled)
            assertTrue(btnLocation.isEnabled)
            assertEquals("GRANT STORAGE ACCESS", btnStorage.text)
        }
    }

    @Test
    fun grantFiles_showsExplanationDialog() {
        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(false)

        val scenario = ActivityScenario.launch(OnboardingActivity::class.java)
        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.btn_grant_files).performClick()

            val dialog = ShadowAlertDialog.getLatestAlertDialog()
            assertNotNull(dialog)

            val shadowDialog = Shadows.shadowOf(dialog)
            assertEquals("Storage Access", shadowDialog.title)
        }
    }

    @Test
    fun finishSetup_whenIncomplete_showsToast() {
        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(false)

        val scenario = ActivityScenario.launch(OnboardingActivity::class.java)
        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.btn_finish_setup).performClick()

            assertEquals("Permissions are required for Protocol Zero", ShadowToast.getTextOfLatestToast())
            assertTrue(!activity.isFinishing)
        }
    }

    @Test
    fun finishSetup_whenComplete_launchesBatteryActivity() {
        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(true)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val shadowApp = Shadows.shadowOf(context as android.app.Application)
        shadowApp.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val scenario = ActivityScenario.launch(OnboardingActivity::class.java)
        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.btn_finish_setup).performClick()

            val shadowActivity = Shadows.shadowOf(activity)
            val intent = shadowActivity.nextStartedActivity

            assertNotNull(intent)
            assertEquals(BatteryActivity::class.java.name, intent.component?.className)
            assertTrue(activity.isFinishing)
        }
    }

    @Test
    fun onResume_updatesUI_whenPermissionsGranted() {
        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(false)

        val scenario = ActivityScenario.launch(OnboardingActivity::class.java)

        mockEnvironment.`when`<Boolean> { Environment.isExternalStorageManager() }
            .thenReturn(true)

        scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED)
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)

        scenario.onActivity { activity ->
            val btnStorage = activity.findViewById<Button>(R.id.btn_grant_files)
            assertFalse(btnStorage.isEnabled)
            assertEquals("STORAGE SECURED", btnStorage.text)
        }
    }
}
