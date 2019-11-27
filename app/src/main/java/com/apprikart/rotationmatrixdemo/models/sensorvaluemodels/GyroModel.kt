package com.apprikart.rotationmatrixdemo.models.sensorvaluemodels

data class GyroModel(
    var sensorName: String,
    var frequency: String,
    var timeStamp: Double,
    var xVal: String,
    var yVal: String,
    var zVal: String
) : Comparable<GyroModel> {
    override fun compareTo(other: GyroModel): Int {
        return (timeStamp - other.timeStamp).toInt()
    }
}