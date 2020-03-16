package com.apprikart.rotationmatrixdemo.models

data class DistanceModel(
    val lastTimeStamp: Double,
    val currentTimeStamp: Double,
    val distanceAsIs: Double,
    val distanceAsIsHp: Double,
    val speedAsIs: Double,
    val speedAsIsHp: Double,
    val totalDistanceAsIs: Double,
    val totalDistanceAsIsHp: Double,
    val totalDistanceGeoFiltered: Double,
    val totalDistanceGeoFilteredHp: Double
)