package com.ghostbattery.ui.secure

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostbattery.R
import com.ghostbattery.data.PrefsManager

class AppPickerActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        prefsManager = PrefsManager(this)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_app_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load installed apps
        val pm = packageManager
        val installedApps = pm.getInstalledPackages(0)
            .filter { (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 } // Exclude system apps
            .map { AppItem(
                it.packageName,
                it.applicationInfo.loadLabel(pm).toString(),
                it.applicationInfo.loadIcon(pm),
                prefsManager.targetApps.contains(it.packageName)
            )}
            .sortedBy { it.label }

        adapter = AppAdapter(installedApps)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btn_save_selection).setOnClickListener {
            saveSelection()
        }
    }

    private fun saveSelection() {
        val selectedPackages = adapter.getSelectedPackages()
        prefsManager.targetApps = selectedPackages
        finish()
    }

    // --- Inner Classes for UI ---
    data class AppItem(val packageName: String, val label: String, val icon: android.graphics.drawable.Drawable, var isSelected: Boolean)

    inner class AppAdapter(private val apps: List<AppItem>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        fun getSelectedPackages(): List<String> = apps.filter { it.isSelected }.map { it.packageName }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.iv_app_icon)
            val label: TextView = view.findViewById(R.id.tv_app_label)
            val checkbox: CheckBox = view.findViewById(R.id.cb_app_select)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_picker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            holder.label.text = app.label
            holder.icon.setImageDrawable(app.icon)
            holder.checkbox.isChecked = app.isSelected

            holder.itemView.setOnClickListener {
                app.isSelected = !app.isSelected
                holder.checkbox.isChecked = app.isSelected
            }
            holder.checkbox.setOnClickListener {
                app.isSelected = (it as CheckBox).isChecked
            }
        }

        override fun getItemCount() = apps.size
    }
}
