package com.apprikart.rotationmatrixdemo.models.sensorvaluemodels

import com.apprikart.rotationmatrixdemo.Utils

data class LAAfterRotation(
    val name: String = Utils.LINEAR_ACCELERATION_AFTER_ROTATION,
    var timeStamp: Double,
    var xVal: String,
    var yVal: String,
    var zVal: String
):Comparable<LAAfterRotation>{
    override fun compareTo(other: LAAfterRotation): Int {
        return (timeStamp - other.timeStamp).toInt()
    }
}

