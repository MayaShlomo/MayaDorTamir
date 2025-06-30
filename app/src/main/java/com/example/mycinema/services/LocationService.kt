// LocationService.kt
package com.example.mycinema.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L // 10 seconds
    ).apply {
        setMinUpdateDistanceMeters(50f) // 50 meters
        setMaxUpdateDelayMillis(30000L) // 30 seconds
    }.build()

    /**
     * בדיקה אם יש הרשאות מיקום
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * קבלת המיקום הנוכחי
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext null
        }

        return@withContext try {
            Tasks.await(fusedLocationClient.lastLocation)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * חישוב מרחק בין שתי נקודות (בקילומטרים)
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return (results[0] / 1000.0) // המרה לקילומטרים
    }

    /**
     * בדיקה אם נקודה נמצאת ברדיוס מסוים
     */
    fun isWithinRadius(
        centerLat: Double, centerLon: Double,
        pointLat: Double, pointLon: Double,
        radiusKm: Double
    ): Boolean {
        val distance = calculateDistance(centerLat, centerLon, pointLat, pointLon)
        return distance <= radiusKm
    }
}
