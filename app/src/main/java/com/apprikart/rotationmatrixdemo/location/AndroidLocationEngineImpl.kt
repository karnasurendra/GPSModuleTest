package com.apprikart.rotationmatrixdemo.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper

class AndroidLocationEngineImpl(context: Context) :
    LocationEngineImpl<LocationListener> {

    private var currentProvider = "passive"
    private var locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    companion object {

        class AndroidLocationEngineCallbackTransport(private val locationEngineCallback: LocationEngineCallback<LocationEngineResult>) :
            LocationListener {

            override fun onLocationChanged(p0: Location?) {
                p0?.let { LocationEngineResult.create(it) }?.let {
                    locationEngineCallback.onSuccess(it)
                }
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                TODO("not implemented")
            }

            override fun onProviderEnabled(p0: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onProviderDisabled(p0: String?) {
                locationEngineCallback.onFailure(Exception("Current provider disabled"))
            }
        }


    }

    private fun getBestProvider(i: Int): String {
        return (if (i != 3) locationManager.getBestProvider(getCriteria(i), true) else null)
            ?: "passive"
    }

    private fun getCriteria(i: Int): Criteria {
        val criteria = Criteria()
        criteria.accuracy = priorityToAccuracy(i)
        criteria.isCostAllowed = true
        criteria.powerRequirement = priorityToPowerRequirement(i)
        return criteria
    }

    private fun priorityToAccuracy(i: Int): Int {
        return when (i) {
            0, 1 -> 1
            else -> 2
        }
    }

    private fun priorityToPowerRequirement(i: Int): Int {
        return when (i) {
            0 -> 3
            1 -> 2
            else -> 1
        }
    }


    override fun getLastLocation(locationEngineCallback: LocationEngineCallback<LocationEngineResult>) {
        val lastLocationFor = getLastLocationFor(currentProvider)
        if (lastLocationFor != null) {
            locationEngineCallback.onSuccess(LocationEngineResult.create(lastLocationFor))
            return
        }

        for (provider in locationManager.allProviders) {
            val lastLocation = getLastLocationFor(provider)
            if (lastLocation != null) {
                locationEngineCallback.onSuccess(LocationEngineResult.create(lastLocation))
                return
            }
        }
        locationEngineCallback.onFailure(Exception("Last location unavailable"))
    }


    @SuppressLint("MissingPermission")
    fun getLastLocationFor(name: String): Location? {
        return if (locationManager.getLastKnownLocation(name) != null)
            locationManager.getLastKnownLocation(name)
        else
            null
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent) {
        locationManager.removeUpdates(pendingIntent)
    }

    override fun removeLocationUpdates(t: LocationListener) {
        locationManager.removeUpdates(t)
    }

    @SuppressLint("MissingPermission")
    override fun requestLocationUpdates(
        locationEngineRequest: LocationEngineRequest,
        pendingIntent: PendingIntent
    ) {
        currentProvider = getBestProvider(locationEngineRequest.getPrority())
        locationManager.requestLocationUpdates(currentProvider,locationEngineRequest.getInterval(),locationEngineRequest.getDisplacement(),pendingIntent)
    }

    @SuppressLint("MissingPermission")
    override fun requesstLocationUpdates(
        locationEngineRequest: LocationEngineRequest,
        t: LocationListener,
        looper: Looper
    ) {
        currentProvider = getBestProvider(locationEngineRequest.getPrority())
        locationManager.requestLocationUpdates(currentProvider,locationEngineRequest.getInterval(),locationEngineRequest.getDisplacement(),t,looper)
    }

    override fun createListener(locationEngineCallback: LocationEngineCallback<LocationEngineResult>): LocationListener {
        return AndroidLocationEngineCallbackTransport(locationEngineCallback)
    }


}