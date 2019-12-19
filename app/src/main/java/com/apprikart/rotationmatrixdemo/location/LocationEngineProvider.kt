package com.apprikart.rotationmatrixdemo.location

import android.content.Context
import com.google.android.gms.common.internal.service.Common
import com.google.android.gms.location.GeofencingClient

class LocationEngineProvider {


    companion object {
        private val GOOGLE_API_AVAILABILITY =
            "com.google.android.gms.common.GoogleApiAvailability"
        private val GOOGLE_LOCATION_SERVICES =
            "com.google.android.gms.location.LocationServices"

        /*fun getBestLocationEngine(context: Context): LocationEngine {
            LocationUtils.checkNotNull(context, "context == null")

            var isOnClassPath = LocationUtils.isOnClassPath(GOOGLE_LOCATION_SERVICES)
            if (LocationUtils.isOnClassPath(GOOGLE_API_AVAILABILITY)) {
            }
        }*/

    }


}