package com.apprikart.rotationmatrixdemo.models.sensorvaluemodels

data class RotationVectorModel(
    var sensorName: String,
    var frequency: String,
    var timeStamp: Double,
    var xVal: String,
    var yVal: String,
    var zVal: String
) : Comparable<RotationVectorModel> {
    override fun compareTo(other: RotationVectorModel): Int {
        return (timeStamp - other.timeStamp).toInt()
    }
}