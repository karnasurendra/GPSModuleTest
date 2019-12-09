package com.apprikart.rotationmatrixdemo.viewmodels

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.filters.Coordinates
import com.apprikart.rotationmatrixdemo.filters.GPSAccKalmanFilter
import com.apprikart.rotationmatrixdemo.loggers.GeohashRTFilter
import com.apprikart.rotationmatrixdemo.models.SensorGpsDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainViewModel(
    application: Application,
    var gpsAccKalmanFilter: GPSAccKalmanFilter,
    var geohashRTFilter: GeohashRTFilter
) : AndroidViewModel(application) {

    var geoValues = MutableLiveData<String>()
    var isTaskLooping = false

    /*fun getValues(): MutableLiveData<String> {
        return geoValues
    }*/

    fun createTextFiles(dir: File, textFileName: String) {
        val file = File(dir.absolutePath, textFileName)
        if (!file.exists()) {
            file.createNewFile()
            writeToFileInitially(file)
        }
    }

    private fun writeToFileInitially(file: File) {
        val fileOutputStream = FileOutputStream(file, true)
        val outputStreamWriter = OutputStreamWriter(fileOutputStream)
        outputStreamWriter.append("[")
        outputStreamWriter.close()
        fileOutputStream.close()
    }

    fun initSensorDataLoopTask(mSensorDataQueue: Queue<SensorGpsDataItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            // Minimum time interval between each estimate position calculation in Millis
//            delay(5000)
            // This is to Check whether the parallel thread is running or not in Activity
            isTaskLooping = true

            var sdi: SensorGpsDataItem
            var lastTimeStamp = 0.0

            while (!mSensorDataQueue.isEmpty()) {

                sdi = mSensorDataQueue.poll()!!
                if (sdi.timestamp < lastTimeStamp) {
                    continue
                }
                lastTimeStamp = sdi.timestamp
                // If Location is not triggered, it will be Not Initialized
                if (sdi.gpsLat == SensorGpsDataItem.NOT_INITIALIZED) {
                    handlePredict(sdi)
                } else {
                    handleUpdate(sdi)
                    val location = locationAfterUpdateStep(sdi)
                    onLocationChangedImp(location)
                }
            }

            isTaskLooping = false

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

        Log.d(
            "KalmanFilter::",
            "Checking Sensor Values onLocationChanged Imp ${System.nanoTime()} \n" +
                    "Location Tag ${location.provider} \n" +
                    "${geohashRTFilter.getDistanceGeoFiltered()} \n" +
                    "${geohashRTFilter.getDistanceGeoFilteredHP()} \n" +
                    "${geohashRTFilter.getDistanceAsIs()} \n" +
                    "${geohashRTFilter.getDistanceAsIsHP()}"
        )


        geoValues.postValue(
            "Distance Geo : ${geohashRTFilter.getDistanceGeoFiltered()} \n" +
                    "Distance Geo HP : ${geohashRTFilter.getDistanceGeoFilteredHP()} \n" +
                    "DistanceAsIs : ${geohashRTFilter.getDistanceAsIs()} \n" +
                    "DistanceAsIs HP : ${geohashRTFilter.getDistanceAsIsHP()}"
        )


    }

    private fun locationAfterUpdateStep(sdi: SensorGpsDataItem): Location {

        val loc = Location(Utils.LOCATION_FROM_FILTER)

        // In Filter values to be in Meters, So by using the metersToGeoPoint getting the geo points i.e latitude and longitude
        val geoPoint = Coordinates.metersToGeoPoint(
            gpsAccKalmanFilter.getCurrentX(),
            gpsAccKalmanFilter.getCurrentY()
        )

        loc.latitude = geoPoint.Latitude
        loc.longitude = geoPoint.Longitude
        loc.altitude = sdi.gpsAlt
        val xVel = gpsAccKalmanFilter.getCurrentXVel()
        val yVel = gpsAccKalmanFilter.getCurrentYVel()

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

    private fun handlePredict(sdi: SensorGpsDataItem) {
        gpsAccKalmanFilter.predict(sdi.timestamp, sdi.absEastAcc, sdi.absNorthAcc)
    }

    private fun handleUpdate(sdi: SensorGpsDataItem) {
        val xVel = sdi.speed * cos(sdi.course)
        val yVel = sdi.speed * sin(sdi.course)

        gpsAccKalmanFilter.update(
            sdi.timestamp,
            Coordinates.longitudeToMeters(sdi.gpsLon),
            Coordinates.latitudeToMeters(sdi.gpsLat),
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
            private var gpsAccKalmanFilter: GPSAccKalmanFilter,
            private var geohashRTFilter: GeohashRTFilter
        ) :
            ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    MainViewModel(application, gpsAccKalmanFilter, geohashRTFilter) as T
                } else {
                    throw IllegalArgumentException("ViewModel not found")
                }
            }
        }
    }

}