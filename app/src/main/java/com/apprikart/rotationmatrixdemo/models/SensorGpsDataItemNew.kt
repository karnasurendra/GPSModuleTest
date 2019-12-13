package com.apprikart.rotationmatrixdemo.models

import androidx.annotation.NonNull
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by Karna on 2/13/18.
 */
data class SensorGpsDataItemNew(
    var timestamp: Double,
    var gpsLat: Double, var gpsLon: Double, var gpsAlt: Double,
    var absNorthAcc: Double, var absEastAcc: Double, var absUpAcc: Double,
    var speed: Double, var course: Double,
    var posErr: Double, var velErr: Double,
    val declination: Double
) : Comparable<SensorGpsDataItemNew> {

    override fun compareTo(@NonNull other: SensorGpsDataItemNew): Int {
        return (timestamp - other.timestamp).toInt()
    }

    companion object {
        const val NOT_INITIALIZED = 361.0
    }

    init {
        absNorthAcc =
            absNorthAcc * cos(declination) + absEastAcc * sin(declination)
        absEastAcc =
            absEastAcc * cos(declination) - absNorthAcc * sin(declination)
    }
}