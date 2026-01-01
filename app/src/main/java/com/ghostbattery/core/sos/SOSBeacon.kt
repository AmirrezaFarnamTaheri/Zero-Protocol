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
        // Uses the last known location to be instantaneous
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    private fun openMessagingApp(phone: String, text: String) {
        // Targets WhatsApp explicitly, falls back to SMS if needed
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$text")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic SMS
            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.data = Uri.parse("sms:$phone")
            smsIntent.putExtra("sms_body", text)
            smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(smsIntent)
        }
    }
}
