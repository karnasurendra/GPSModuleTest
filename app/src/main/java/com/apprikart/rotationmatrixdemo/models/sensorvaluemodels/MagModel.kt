package com.apprikart.rotationmatrixdemo.models.sensorvaluemodels

data class MagModel(
    var sensorName: String,
    var frequency: String,
    var timeStamp: Double,
    var xVal: String,
    var yVal: String,
    var zVal: String
) : Comparable<MagModel> {
    override fun compareTo(other: MagModel): Int {
        return (timeStamp - other.timeStamp).toInt()
    }
}