package com.ghostbattery.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PrefsManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secret_battery_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrefsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // --- SOS Configuration ---
    var sosNumber: String
        get() = sharedPreferences.getString("sos_number", "") ?: ""
        set(value) = sharedPreferences.edit().putString("sos_number", value).apply()

    var sosMessage: String
        get() = sharedPreferences.getString("sos_message", "Emergency Protocol Active.") ?: "Emergency Protocol Active."
        set(value) = sharedPreferences.edit().putString("sos_message", value).apply()

    // --- Target Apps Configuration ---
    // Stored as a comma-separated string because Sets can be buggy in SharedPreferences
    var targetApps: List<String>
        get() = sharedPreferences.getString("target_apps", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        set(value) = sharedPreferences.edit().putString("target_apps", value.joinToString(",")).apply()

    // --- Custom Folder Targets ---
    // Stores absolute paths e.g., "/sdcard/Documents/Secret_Project"
    var customFolders: Set<String>
        get() = sharedPreferences.getStringSet("custom_folders", emptySet()) ?: emptySet()
        set(value) = sharedPreferences.edit().putStringSet("custom_folders", value).apply()

    // Helper to add a single folder
    fun addCustomFolder(path: String) {
        val current = customFolders.toMutableSet()
        current.add(path)
        customFolders = current
    }

    // Helper to remove a folder
    fun removeCustomFolder(path: String) {
        val current = customFolders.toMutableSet()
        current.remove(path)
        customFolders = current
    }

    // --- Panic Trigger Pin ---
    // Default secret code is "5555" if not set
    var panicPin: String
        get() = sharedPreferences.getString("panic_pin", "5555") ?: "5555"
        set(value) = sharedPreferences.edit().putString("panic_pin", value).apply()

    var isPanicModeActive: Boolean
        get() = sharedPreferences.getBoolean("panic_mode_active", false)
        set(value) = sharedPreferences.edit().putBoolean("panic_mode_active", value).apply()

    // --- Allowed Accessibility Packages ---
    // List of packages the Accessibility Service is allowed to interact with
    var allowedAccessibilityPackages: Set<String>
        get() = sharedPreferences.getStringSet("allowed_accessibility_packages", emptySet()) ?: emptySet()
        set(value) = sharedPreferences.edit().putStringSet("allowed_accessibility_packages", value).apply()
}
