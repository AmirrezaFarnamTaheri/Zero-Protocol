package com.ghostbattery.core.sos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class SOSBeacon(private val context: Context) {

    suspend fun sendEmergencySignal(contactNumber: String, message: String) {
        val location = getLastLocation()
        val mapsLink = if (location != null) {
            "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
        } else {
            "Location Unavailable"
        }

        val fullMessage = "$message\n\nMy Last Location:\n$mapsLink"
        openMessagingApp(contactNumber, fullMessage)
    }

    private suspend fun getLastLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val last = try {
            fusedLocationClient.lastLocation.await()
        } catch (_: Exception) {
            null
        }
        if (last != null) return last

        return try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()
        } catch (_: Exception) {
            null
        }
    }

    private fun openMessagingApp(phone: String, text: String) {
        // Prefer direct WhatsApp send, fall back to SMS
        try {
            val waIntent = Intent(Intent.ACTION_SEND).apply {
                // Use ACTION_SEND for sharing content.
                type = "text/plain"
                // Explicitly set the package to ensure it's sent via WhatsApp.
                setPackage("com.whatsapp")
                // The "jid" extra is used to specify the recipient's phone number in the format required by WhatsApp.
                // The "@s.whatsapp.net" suffix is the standard for WhatsApp JIDs.
                putExtra("jid", "$phone@s.whatsapp.net")
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(waIntent)
        } catch (e: Exception) {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phone")
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
        }
    }
}
