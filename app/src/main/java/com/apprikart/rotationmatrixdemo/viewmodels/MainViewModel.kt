package com.apprikart.rotationmatrixdemo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apprikart.rotationmatrixdemo.filters.GPSAccKalmanFilter
import com.apprikart.rotationmatrixdemo.models.SensorGpsDataItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import javax.inject.Inject

class MainViewModel(
    application: Application,
    var gpsAccKalmanFilter: GPSAccKalmanFilter
) : AndroidViewModel(application) {

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
        viewModelScope.launch {
            // Minimum time interval between each estimate position calculation in Millis
            delay(1000)

            var sdi: SensorGpsDataItem
            var lastTimeStamp = 0.0

            while (mSensorDataQueue.poll().also { sdi = it } != null) {
                if (sdi.timestamp < lastTimeStamp) {
                    continue
                }
                lastTimeStamp = sdi.timestamp

                // If Location is not triggered, it will be Not Initialized
                if (sdi.gpsLat == SensorGpsDataItem.NOT_INITIALIZED) {
                    handlePredict(sdi)
                } else {

                }

            }


        }
    }

    private fun handlePredict(sdi: SensorGpsDataItem) {
        gpsAccKalmanFilter.predict(sdi.timestamp, sdi.absEastAcc, sdi.absNorthAcc)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        class Factory @Inject constructor(
            var application: Application,
            var gpsAccKalmanFilter: GPSAccKalmanFilter
        ) :
            ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    MainViewModel(application, gpsAccKalmanFilter) as T
                } else {
                    throw IllegalArgumentException("ViewModel not found")
                }
            }

        }
    }

}