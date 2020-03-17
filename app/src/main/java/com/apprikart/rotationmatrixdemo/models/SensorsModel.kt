package com.apprikart.rotationmatrixdemo.models

data class SensorsModel(
    val sensorName: String,
    val timestamp: Long,
    val x: Double,
    val y: Double,
    val z: Double,
    val distance: Double
)