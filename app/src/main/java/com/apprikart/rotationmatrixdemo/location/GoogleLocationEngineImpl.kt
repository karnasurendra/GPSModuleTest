package com.apprikart.rotationmatrixdemo.location

import android.app.PendingIntent
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback

class GoogleLocationEngineImpl : LocationEngineImpl<LocationCallback> {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun createListener(locationEngineCallback: LocationEngineCallback<LocationEngineResult>): LocationCallback {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLastLocation(locationEngineCallback: LocationEngineCallback<LocationEngineResult>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeLocationUpdates(t: LocationCallback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestLocationUpdates(
        locationEngineRequest: LocationEngineRequest,
        pendingIntent: PendingIntent
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requesstLocationUpdates(
        locationEngineRequest: LocationEngineRequest,
        t: LocationCallback,
        looper: Looper
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}