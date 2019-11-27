package com.apprikart.rotationmatrixdemo.models.sensorvaluemodels

data class AccModel(
    var sensorName: String,
    var frequency: String,
    var timeStamp: Double,
    var xVal: String,
    var yVal: String,
    var zVal: String
) : Comparable<AccModel> {
    // As we are using this Model for Queue, It requires some value to compare the pairs
    // so we implemented Comparable
    override fun compareTo(other: AccModel): Int {
        return (timeStamp - other.timeStamp).toInt()
    }
}