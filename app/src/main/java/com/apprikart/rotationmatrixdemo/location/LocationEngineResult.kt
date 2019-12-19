package com.apprikart.rotationmatrixdemo.location

import android.location.Location
import com.google.android.gms.location.LocationResult
import java.util.*
import kotlin.collections.ArrayList

class LocationEngineResult(list: List<Location>) {

    private var locations: List<Location> = Collections.unmodifiableList(list)

    companion object {
        fun create(list: List<Location>): LocationEngineResult {
            LocationUtils.checkNotNull(list, "locations can't be null")
            return LocationEngineResult(list)
        }

        fun create(location: Location): LocationEngineResult {
            LocationUtils.checkNotNull(location, "location can't be null")
            val arrayList = ArrayList<Location>()
            arrayList.add(location)
            return LocationEngineResult(arrayList)
        }
    }

}