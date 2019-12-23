package com.apprikart.rotationmatrixdemo.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.filters.CoordinatesNew
import com.apprikart.rotationmatrixdemo.filters.GPSAccKalmanFilterNew
import com.apprikart.rotationmatrixdemo.location.LocationEngine
import com.apprikart.rotationmatrixdemo.location.LocationEngineProvider
import com.apprikart.rotationmatrixdemo.location.LocationEngineRequest
import com.apprikart.rotationmatrixdemo.location.LocationUpdateFromEngine
import com.apprikart.rotationmatrixdemo.loggers.GeohashRTFilter
import com.apprikart.rotationmatrixdemo.models.SensorGpsDataItemNew
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainViewModel(
    application: Application,
    var gpsAccKalmanFilterNew: GPSAccKalmanFilterNew,
    var geohashRTFilter: GeohashRTFilter
) : AndroidViewModel(application) {

    var geoValues = MutableLiveData<String>()
    var needTerminate = true
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationUpdateFromEngine: LocationUpdateFromEngine
    var location = MutableLiveData<Location>()

    private fun buildEngineRequest(): LocationEngineRequest {
        return LocationEngineRequest.Builder(Utils.UPDATE_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setFastestInterval(Utils.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .build()
    }

    /**
     * Location is implemented from Map box SDK, which is of Open Source
     * <p>
     * (c) https://github.com/mapbox/mapbox-events-android/tree/master/liblocation/src/main/java/com/mapbox/android/core/location
     */
    @SuppressLint("MissingPermission")
    fun initLocation() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(getApplication())
        locationUpdateFromEngine = LocationUpdateFromEngine(location)
        val request = buildEngineRequest()
        locationEngine.requestLocationUpdates(request, locationUpdateFromEngine, null)
    }

    fun removeLocation() {
        locationEngine.removeLocationUpdates(locationUpdateFromEngine)
    }

    fun initSensorDataLoopTask(mSensorDataQueueNew: Queue<SensorGpsDataItemNew>) {
        viewModelScope.launch(Dispatchers.IO) {
            while (!needTerminate) {
                delay(500)
                var sdi: SensorGpsDataItemNew
                var lastTimeStamp = 0.0
                while (!mSensorDataQueueNew.isEmpty()) {
                    sdi = mSensorDataQueueNew.poll()!!
                    if (sdi.timestamp < lastTimeStamp) {
                        continue
                    }
                    lastTimeStamp = sdi.timestamp
                    // If Location is not triggered, it will be Not Initialized
                    if (sdi.gpsLat == SensorGpsDataItemNew.NOT_INITIALIZED) {
                        handlePredict(sdi)
                    } else {
                        handleUpdate(sdi)
                        val location = locationAfterUpdateStep(sdi)
                        onLocationChangedImp(location)
                    }
                }
            }
        }
    }

    private fun onLocationChangedImp(location: Location) {

        // Location provider will change in GeoHashRTFilter class
        if (location.latitude == 0.0 ||
            location.longitude == 0.0 ||
            location.provider != Utils.LOCATION_FROM_FILTER
        ) {
            return
        }
        geoValues.postValue(
            "Distance Geo : ${geohashRTFilter.getDistanceGeoFiltered()} \n" +
                    "Distance Geo HP : ${geohashRTFilter.getDistanceGeoFilteredHP()} \n" +
                    "DistanceAsIs : ${geohashRTFilter.getDistanceAsIs()} \n" +
                    "DistanceAsIs HP : ${geohashRTFilter.getDistanceAsIsHP()}"
        )

    }

    private fun locationAfterUpdateStep(sdi: SensorGpsDataItemNew): Location {

        val loc = Location(Utils.LOCATION_FROM_FILTER)

        // In Filter values to be in Meters, So by using the metersToGeoPoint getting the geo points i.e latitude and longitude
        val geoPoint = CoordinatesNew.metersToGeoPoint(
            gpsAccKalmanFilterNew.getCurrentX(),
            gpsAccKalmanFilterNew.getCurrentY()
        )

        loc.latitude = geoPoint.Latitude
        loc.longitude = geoPoint.Longitude
        loc.altitude = sdi.gpsAlt
        val xVel = gpsAccKalmanFilterNew.getCurrentXVel()
        val yVel = gpsAccKalmanFilterNew.getCurrentYVel()

        val speed =
            sqrt(xVel * xVel + yVel * yVel) //scalar speed without bearing Note : Scalar means one dimensional quantity

        loc.bearing = sdi.course.toFloat()
        loc.speed = speed.toFloat()
        loc.time = System.currentTimeMillis()
        loc.elapsedRealtimeNanos = System.nanoTime()
        loc.accuracy = sdi.posErr.toFloat()

        geohashRTFilter.filter(loc)

        return loc

    }

    private fun handlePredict(sdi: SensorGpsDataItemNew) {
        gpsAccKalmanFilterNew.predict(sdi.timestamp, sdi.absEastAcc, sdi.absNorthAcc)
    }

    private fun handleUpdate(sdi: SensorGpsDataItemNew) {
        val xVel = sdi.speed * cos(sdi.course)
        val yVel = sdi.speed * sin(sdi.course)

        gpsAccKalmanFilterNew.update(
            sdi.timestamp,
            CoordinatesNew.longitudeToMeters(sdi.gpsLon),
            CoordinatesNew.latitudeToMeters(sdi.gpsLat),
            xVel,
            yVel,
            sdi.posErr,
            sdi.velErr
        )

    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        class Factory @Inject constructor(
            private var application: Application,
            private var gpsAccKalmanFilterNew: GPSAccKalmanFilterNew,
            private var geohashRTFilter: GeohashRTFilter
        ) :
            ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    MainViewModel(application, gpsAccKalmanFilterNew, geohashRTFilter) as T
                } else {
                    throw IllegalArgumentException("ViewModel not found")
                }
            }
        }
    }

}