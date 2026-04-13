package com.mayokunadeniyi.instantweather.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import com.mayokunadeniyi.instantweather.data.model.LocationModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Created by Mayokun Adeniyi on 15/03/2026.
 */

open class LocationManager(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    open fun getLocationUpdates(): Flow<LocationModel> = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySend(LocationModel(location.longitude, location.latitude))
                }
            }
        }

        // Get initial location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                trySend(LocationModel(it.longitude, it.latitude))
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }.distinctUntilChanged()

    @SuppressLint("MissingPermission")
    open fun getLastKnownLocation(onLocation: (LocationModel?) -> Unit) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    onLocation(LocationModel(it.longitude, it.latitude))
                } ?: onLocation(null)
            }
        } catch (e: Exception) {
            onLocation(null)
        }
    }


    companion object {
        val locationRequest: LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).apply {
            setMinUpdateIntervalMillis(5000L)
        }.build()
    }
}
