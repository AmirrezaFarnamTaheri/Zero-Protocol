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
                "🚨 CRITICAL: READ FIRST",
                """
                Protocol Zero is a fail-safe panic system. It is designed to run when you are under duress.

                DO NOT test the 'Execute Sequence' unless you are prepared to lose data. Use 'Drill Mode' in Settings for practice.
                """.trimIndent()
            ),
            HelpItem(
                "👻 STEALTH MODE (CAMOUFLAGE)",
                """
                1. THE DISGUISE:
                - The app masquerades as 'System Battery Health'.
                - It displays real voltage and charging status to pass a casual inspection.

                2. ACTIVATION:
                - Tap the 'VOLTAGE' text (e.g., '4200 mV') exactly 5 times rapidly.
                - This unlocks the Red 'Panic Dashboard'.

                3. ICON SWITCHER:
                - Go to Settings > Change Icon to disguise the app as a Calculator or Weather app.
                """.trimIndent()
            ),
            HelpItem(
                "⚡ THE PANIC PROTOCOL",
                """
                When you initiate the sequence (or tap individual buttons), the following happens:

                1. SOS BEACON (Network):
                - Sends a pre-defined WhatsApp message with your location to your trusted contact.
                - Uses Accessibility to auto-click 'Send'.

                2. DATA INCINERATOR (Background):
                - Overwrites file headers with random noise before deleting them.
                - Targets: Gallery, Downloads, Documents, WhatsApp Media.

                3. APP PURGE (Foreground):
                - Uninstalls selected sensitive apps (e.g., Signal, Telegram).
                - Uses Accessibility to auto-click 'Uninstall'/'OK'.

                4. SELF DESTRUCT:
                - The app uninstalls itself to remove evidence of the tool.
                """.trimIndent()
            ),
            HelpItem(
                "🛡️ SETUP REQUIREMENTS",
                """
                For the automation to work, you must grant:

                1. ACCESSIBILITY SERVICE:
                - Required to click 'Uninstall' and 'Send' buttons for you.

                2. ALL FILES ACCESS:
                - Required to permanently delete files without moving them to Trash.

                3. DISPLAY OVER OTHER APPS:
                - Required to keep the panic screen visible.
                """.trimIndent()
            ),
             HelpItem(
                "🧪 DRILL MODE",
                """
                Located in Settings.

                - Simulates the entire panic sequence visually.
                - DOES NOT delete files or uninstall apps.
                - Use this to train your muscle memory.
                """.trimIndent()
            ),
            HelpItem(
                "❓ TROUBLESHOOTING",
                """
                - "Automation stops working": Android may kill the Accessibility Service. Go to Settings > Accessibility and toggle 'System Battery Health' OFF and ON.
                - "Files in Trash": If you see deleted files in the Trash/Recycle Bin, it means 'All Files Access' was not granted. The app fell back to standard deletion.
                """.trimIndent()
            )
        )
    }
}
