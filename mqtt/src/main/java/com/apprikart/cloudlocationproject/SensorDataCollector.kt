package com.apprikart.cloudlocationproject

import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager

abstract class SensorDataCollector(private var sensorManager: SensorManager) : SensorEventListener {

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Not doing Anything
    }

    var sensorsList: MutableList<Sensor> = ArrayList()

    // Register your sensors by calling below method
    fun start() {
        for (sensor: Sensor in sensorsList) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        // UnRegister the sensors by calling the below method
        for (sensor: Sensor in sensorsList) {
            sensorManager.unregisterListener(this, sensor)
        }
    }

}