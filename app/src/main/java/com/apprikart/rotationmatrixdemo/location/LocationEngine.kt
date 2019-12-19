package com.apprikart.rotationmatrixdemo.location

import android.app.PendingIntent
import android.os.Looper

interface LocationEngine {

    fun getLastLocation(locationEngineCallback: LocationEngineCallback<LocationEngineResult>)
    fun removeLocationUpdates(pendingIntent: PendingIntent)
    fun removeLocationUpdates(locationEngineCallback: LocationEngineCallback<LocationEngineResult>)
    fun requestLocationUdpdates(
        locationEngineRequest: LocationEngineRequest,
        pendingIntent: PendingIntent
    )

    fun requestLocationUpdates(
        locationEngineRequest: LocationEngineRequest,
        locationEngineCallback: LocationEngineCallback<LocationEngineResult>,
        looper: Looper
    )

}