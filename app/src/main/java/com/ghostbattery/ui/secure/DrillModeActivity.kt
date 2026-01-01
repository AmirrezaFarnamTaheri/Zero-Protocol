package com.ghostbattery.ui.secure

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ghostbattery.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DrillModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panic_dashboard)

        // 1. UI Reskin for Simulation
        findViewById<TextView>(R.id.tv_title).apply {
            text = "DRILL MODE (SIMULATION)"
            setTextColor(Color.CYAN)
        }

        // 2. Setup Dummy Buttons
        setupDrillButtons()

        // 3. Show safety toast
        Toast.makeText(this, "SAFE MODE: No data will be deleted.", Toast.LENGTH_LONG).show()
    }

    private fun setupDrillButtons() {
        findViewById<Button>(R.id.btn_sos).setOnClickListener {
            runSimulation("SOS Beacon")
        }

        findViewById<Button>(R.id.btn_wipe_gallery).setOnClickListener {
            runSimulation("Gallery Incineration")
        }

        findViewById<Button>(R.id.btn_uninstall_apps).setOnClickListener {
            lifecycleScope.launch {
                Toast.makeText(this@DrillModeActivity, "Simulating App Uninstall Loop...", Toast.LENGTH_SHORT).show()
                delay(1000)
                Toast.makeText(this@DrillModeActivity, "Uninstall Dialog 1: Click OK (Simulated)", Toast.LENGTH_SHORT).show()
                delay(2000)
                Toast.makeText(this@DrillModeActivity, "Uninstall Dialog 2: Click OK (Simulated)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun runSimulation(action: String) {
        lifecycleScope.launch {
            Toast.makeText(this@DrillModeActivity, "Starting $action...", Toast.LENGTH_SHORT).show()
            delay(1500) // Fake processing time
            Toast.makeText(this@DrillModeActivity, "$action COMPLETE (Simulated)", Toast.LENGTH_SHORT).show()
        }
    }
}
