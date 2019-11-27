package com.apprikart.rotationmatrixdemo.models.sensorvaluemodels

data class LinearAccModel(
    var sensorName: String,
    var frequency: String,
    var timeStamp: Double,
    var xVal: String,
    var yVal: String,
    var zVal: String
):Comparable<LinearAccModel>{
    override fun compareTo(other: LinearAccModel): Int {
        return (timeStamp - other.timeStamp).toInt()
    }
}