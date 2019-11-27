package com.apprikart.cloudlocationproject

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log

class MadgwikProvider(sensorManager: SensorManager) : SensorDataCollector(sensorManager) {

    private val acc = FloatArray(3)
    private val gyr = FloatArray(3)

    init {
        sensorsList.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
        sensorsList.add(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE))
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, acc, 0, 3)
                for (i in acc.indices){
                    Log.d("MadgwikProvider::", "onSensorChanged Accelerometer $i - Position and Val ${acc[i]}")
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                System.arraycopy(event.values, 0, gyr, 0, 3)
                for (i in gyr.indices){
                    Log.d("MadgwikProvider::", "onSensorChanged Gyroscope $i - Position and Val ${gyr[i]}")
                }
            }
            else -> return
        }
    }
}