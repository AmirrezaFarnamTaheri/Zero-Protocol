package com.ghostbattery.ui.secure

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostbattery.R
import com.ghostbattery.data.PrefsManager
import java.io.File

class FolderPickerActivity : AppCompatActivity() {

    private lateinit var currentDir: File
    private lateinit var adapter: FileAdapter
    private lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_picker)

        prefsManager = PrefsManager(this)
        currentDir = Environment.getExternalStorageDirectory() // Start at /sdcard

        val recyclerView = findViewById<RecyclerView>(R.id.rv_file_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FileAdapter { file -> navigateTo(file) }
        recyclerView.adapter = adapter

        updateUI()

        findViewById<Button>(R.id.btn_select_current_folder).setOnClickListener {
            addCurrentFolder()
        }

        findViewById<Button>(R.id.btn_go_up).setOnClickListener {
            if (currentDir.parentFile != null && currentDir.absolutePath != "/storage/emulated") {
                navigateTo(currentDir.parentFile!!)
            }
        }
    }

    private fun updateUI() {
        findViewById<TextView>(R.id.tv_current_path).text = currentDir.absolutePath
        val dirs = currentDir.listFiles()?.filter { it.isDirectory && !it.isHidden }?.sortedBy { it.name } ?: emptyList()
        adapter.submitList(dirs)
    }

    private fun navigateTo(file: File) {
        currentDir = file
        updateUI()
    }

    private fun addCurrentFolder() {
        prefsManager.addCustomFolder(currentDir.absolutePath)
        Toast.makeText(this, "Target Added: ${currentDir.name}", Toast.LENGTH_SHORT).show()
    }

    // --- Adapter ---
    class FileAdapter(private val onDirClick: (File) -> Unit) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {
        private var files: List<File> = emptyList()

        fun submitList(newFiles: List<File>) {
            files = newFiles
            notifyDataSetChanged()
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            view.findViewById<TextView>(android.R.id.text1).setTextColor(0xFFFFFFFF.toInt()) // White text
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = files[position]
            holder.name.text = "📁 ${file.name}"
            holder.itemView.setOnClickListener { onDirClick(file) }
        }

        override fun getItemCount() = files.size
    }
}
