package com.ghostbattery.ui.help

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostbattery.R
import com.ghostbattery.data.model.HelpItem

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_help_topics)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = HelpAdapter(getHelpTopics())
    }

    private fun getHelpTopics(): List<HelpItem> {
        return listOf(
            HelpItem(
                "🛑 PHASE 0: CRITICAL PRE-REQUISITES",
                """
                Before relying on this app, you MUST configure Android settings:

                1. SECURE FOLDER:
                - Go to Quick Settings (Swipe Down) -> Add 'Secure Folder'.
                - Move ALL sensitive apps (Telegram, Signal) inside it.
                - This app protects the 'Decoy' side; Secure Folder protects the 'Real' side.

                2. LOCKDOWN MODE:
                - Settings -> Lock Screen -> Secure Lock Settings.
                - Enable 'Show Lockdown Option'.
                - Usage: Hold Power + Vol Down to instantly disable fingerprint unlocking.
                """.trimIndent()
            ),
            HelpItem(
                "🔋 PHASE 1: CAMOUFLAGE MASTERY",
                """
                How to access the Dashboard without raising suspicion:

                1. THE DISGUISE:
                - The app looks like a Battery Saver.
                - It shows real voltage/level data to pass inspection.

                2. THE TRIGGER:
                - Tap the text that says "VOLTAGE" (or "4200 mV") exactly 5 times.
                - Do it quickly (within 2 seconds).
                - This unlocks the Red 'Panic Dashboard'.

                3. STEALTH TIP:
                - Turn off Notifications for this app in Android Settings so no 'Permission Granted' alerts appear.
                """.trimIndent()
            ),
            HelpItem(
                "⚙️ PHASE 2: AUTOMATION SETUP",
                """
                The app needs to click buttons for you.

                1. ACCESSIBILITY:
                - When asked, enable 'System Battery Health' in Accessibility settings.
                - If the toggle turns gray/off, reboot the phone.

                2. FILE ACCESS:
                - 'Manage All Files' is REQUIRED to bypass the Recycle Bin.
                - Without this, photos are just moved to 'Trash', not deleted.
                """.trimIndent()
            ),
            HelpItem(
                "🏃 PHASE 3: THE CHASE (EXECUTION)",
                """
                Use this when you have 2-5 minutes (e.g., being pulled over/stopped).

                1. OPEN APP -> TAP 5 TIMES.
                2. TAP 'EXECUTE SEQUENCE'.

                WHAT HAPPENS NEXT?
                - SOS: Sends location to your trusted contact via WhatsApp (Auto-clicks 'Send').
                - INCINERATOR: Background process starts corrupting and deleting files.
                - PURGE: Foreground loop launches Uninstalls.
                - ACTION REQUIRED: Keep the screen ON. If a dialog appears, the app will try to click 'OK'. If it misses, TAP OK YOURSELF.
                """.trimIndent()
            ),
            HelpItem(
                "🔥 PHASE 4: DATA INCINERATION",
                """
                How deletion works in this app:

                - STANDARD DELETE: Just removes the file pointer.
                - INCINERATION (This App):
                  1. Opens the file.
                  2. Overwrites the header (first 4KB) with random noise.
                  3. Deletes the file.
                - RESULT: Even if recovered, the file is unreadable static.
                - SCOPE: Internal Storage + SD Cards + WhatsApp Media + Samsung Trash.
                """.trimIndent()
            ),
            HelpItem(
                "⚠️ TROUBLESHOOTING",
                """
                - "App Crashes on Uninstall": This is normal if the target app is already gone.
                - "SOS didn't send": Check if you have internet. If signal is cut, the message stays queued.
                - "Decoy Trigger won't work": Ensure you are tapping the VOLTAGE text, not the icon.
                """.trimIndent()
            )
        )
    }
}
