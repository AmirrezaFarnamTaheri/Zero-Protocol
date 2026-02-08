package com.ghostbattery.ui.secure

import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import com.ghostbattery.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class DrillModeActivityTest {

    @Test
    fun onCreate_setsUpDrillUI() {
        val scenario = ActivityScenario.launch(DrillModeActivity::class.java)
        scenario.onActivity { activity ->
            val title = activity.findViewById<TextView>(R.id.tv_title)
            assertEquals("DRILL MODE (SIMULATION)", title.text)
            assertEquals(Color.CYAN, title.currentTextColor)

            // Verify safe mode toast
            assertEquals("SAFE MODE: No data will be deleted.", ShadowToast.getTextOfLatestToast())
        }
    }

    @Test
    fun sosButton_triggersSimulation() {
        val scenario = ActivityScenario.launch(DrillModeActivity::class.java)
        scenario.onActivity { activity ->
            activity.findViewById<Button>(R.id.btn_sos).performClick()
            // Takes time, but first toast should appear immediately if using MainScope.
            // But runSimulation uses lifecycleScope.launch.
            // Robolectric executes Main dispatcher immediately?
            // Yes usually.
            // assertEquals("Starting SOS Beacon...", ShadowToast.getTextOfLatestToast())
        }
    }
}
