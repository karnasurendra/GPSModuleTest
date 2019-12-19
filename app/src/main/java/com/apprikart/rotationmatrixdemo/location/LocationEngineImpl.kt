package com.apprikart.rotationmatrixdemo.location

import android.app.PendingIntent
import android.os.Looper

interface LocationEngineImpl<T> {

    fun createListener(locationEngineCallback: LocationEngineCallback<LocationEngineResult>): T
    fun getLastLocation(locationEngineCallback: LocationEngineCallback<LocationEngineResult>)
    fun removeLocationUpdates(pendingIntent: PendingIntent)
    fun removeLocationUpdates(t: T)
    fun requestLocationUpdates(
        locationEngineRequest: LocationEngineRequest,
        pendingIntent: PendingIntent
    )

    fun requesstLocationUpdates(locationEngineRequest: LocationEngineRequest, t: T, looper: Looper)

}