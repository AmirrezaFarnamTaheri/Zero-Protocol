package com.ghostbattery.ui.decoy

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.ghostbattery.R
import com.ghostbattery.data.PrefsManager
import com.ghostbattery.ui.PanicDashboardActivity
import com.ghostbattery.ui.onboarding.OnboardingActivity
import com.ghostbattery.utils.PermissionHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.MockedStatic
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class BatteryActivityTest {

    private lateinit var mockPermissions: MockedStatic<PermissionHelper>

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PrefsManager.getInstance(context)
        prefs.panicPin = "5555"

        mockPermissions = Mockito.mockStatic(PermissionHelper::class.java)
    }

    @org.junit.After
    fun tearDown() {
        mockPermissions.close()
    }

    @Test
    fun onCreate_withMissingPermissions_redirectsToOnboarding() {
        mockPermissions.`when`<Boolean> { PermissionHelper.hasLocationPermission(Mockito.any()) }
            .thenReturn(false)

        val scenario = ActivityScenario.launch(BatteryActivity::class.java)
        scenario.onActivity { activity ->
            val shadowActivity = Shadows.shadowOf(activity)
            val intent = shadowActivity.nextStartedActivity

            assertNotNull("Should launch OnboardingActivity", intent)
            assertEquals(OnboardingActivity::class.java.name, intent.component?.className)
            assertTrue(activity.isFinishing)
        }
    }

    @Test
    fun secretTrigger_5Taps_showsPinDialog() {
        mockPermissions.`when`<Boolean> { PermissionHelper.hasLocationPermission(Mockito.any()) }
            .thenReturn(true)

        val scenario = ActivityScenario.launch(BatteryActivity::class.java)
        scenario.onActivity { activity ->
            val trigger = activity.findViewById<View>(R.id.view_secret_trigger)

            repeat(5) {
                trigger.performClick()
            }

            val dialog = ShadowAlertDialog.getLatestAlertDialog()
            assertNotNull("PIN Dialog should be shown", dialog)
        }
    }

    @Test
    fun pinDialog_correctPin_launchesPanicDashboard() {
        mockPermissions.`when`<Boolean> { PermissionHelper.hasLocationPermission(Mockito.any()) }
            .thenReturn(true)

        val scenario = ActivityScenario.launch(BatteryActivity::class.java)
        scenario.onActivity { activity ->
            val trigger = activity.findViewById<View>(R.id.view_secret_trigger)
            repeat(5) { trigger.performClick() }

            val dialog = ShadowAlertDialog.getLatestAlertDialog() as AlertDialog
            val editText = findEditText(dialog.window?.decorView)

            assertNotNull("EditText should be found in dialog", editText)
            editText?.setText("5555")

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

            val shadowActivity = Shadows.shadowOf(activity)
            val intent = shadowActivity.nextStartedActivity

            assertNotNull("Should launch PanicDashboardActivity", intent)
            assertEquals(PanicDashboardActivity::class.java.name, intent.component?.className)
        }
    }

    private fun findEditText(view: View?): EditText? {
        if (view is EditText) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val found = findEditText(child)
                if (found != null) return found
            }
        }
        return null
    }
}
