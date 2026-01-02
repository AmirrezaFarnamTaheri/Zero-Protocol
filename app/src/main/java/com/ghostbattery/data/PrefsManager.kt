package com.ghostbattery.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PrefsManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = try {
            EncryptedSharedPreferences.create(
                context,
                "secret_battery_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("PrefsManager", "Encrypted prefs init failed, retrying", e)
            // Clear both the prefs file and the crypto keysets used by EncryptedSharedPreferences
            context.deleteSharedPreferences("secret_battery_prefs")
            context.deleteSharedPreferences("__androidx_security_crypto_encrypted_prefs_key_keyset__")
            context.deleteSharedPreferences("__androidx_security_crypto_encrypted_prefs_value_keyset__")

            try {
                EncryptedSharedPreferences.create(
                    context,
                    "secret_battery_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e2: Exception) {
                Log.e("PrefsManager", "Retry prefs init failed, falling back to plain prefs", e2)
                context.getSharedPreferences("secret_battery_prefs_plain_fallback", Context.MODE_PRIVATE)
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
}
