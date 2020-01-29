package com.apprikart.rotationmatrixdemo.location

import android.location.Location
import androidx.lifecycle.MutableLiveData
import java.lang.Exception

class LocationUpdateFromEngine(private val location: MutableLiveData<Location>) :
    LocationEngineCallback<LocationEngineResult> {
    override fun onSuccess(result: LocationEngineResult) {
        location.value = result.lastLocation
    }

    override fun onFailure(exception: Exception) {
    }
}