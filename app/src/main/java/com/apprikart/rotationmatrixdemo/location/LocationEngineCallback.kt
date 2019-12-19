package com.apprikart.rotationmatrixdemo.location

import com.google.android.gms.location.LocationResult
import java.lang.Exception

interface LocationEngineCallback<T> {

    fun onFailure(exception: Exception)

    fun onSuccess(t: T)

}