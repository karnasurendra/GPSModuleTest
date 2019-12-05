package com.apprikart.rotationmatrixdemo.filters

import com.apprikart.rotationmatrixdemo.models.GeoPoint
import kotlin.math.*

/**
 * Created by lezh1k on 2/13/18.
 */
object Coordinates {


    const val EARTH_RADIUS = 6371.0 * 1000.0 // meters

    fun distanceBetween(
        lon1: Double,
        lat1: Double,
        lon2: Double,
        lat2: Double
    ): Double {
        val deltaLon = Math.toRadians(lon2 - lon1)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val a = sin(deltaLat / 2.0).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(deltaLon / 2.0).pow(2.0)
        val c =
            2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS * c
    }

    fun longitudeToMeters(lon: Double): Double {
        val distance = distanceBetween(lon, 0.0, 0.0, 0.0)
        return distance * if (lon < 0.0) -1.0 else 1.0
    }

    fun metersToGeoPoint(
        lonMeters: Double,
        latMeters: Double
    ): GeoPoint {
        val point = GeoPoint(0.0, 0.0)
        val pointEast = pointPlusDistanceEast(point, lonMeters)
        return pointPlusDistanceNorth(pointEast, latMeters)
    }

    fun latitudeToMeters(lat: Double): Double {
        val distance = distanceBetween(0.0, lat, 0.0, 0.0)
        return distance * if (lat < 0.0) -1.0 else 1.0
    }

    private fun getPointAhead(
        point: GeoPoint,
        distance: Double,
        azimuthDegrees: Double
    ): GeoPoint {
        val radiusFraction = distance / EARTH_RADIUS
        val bearing = Math.toRadians(azimuthDegrees)
        val lat1 = Math.toRadians(point.Latitude)
        val lng1 = Math.toRadians(point.Longitude)
        val lat2Part1 =
            sin(lat1) * cos(radiusFraction)
        val lat2Part2 =
            cos(lat1) * sin(radiusFraction) * cos(
                bearing
            )
        val lat2 = asin(lat2Part1 + lat2Part2)
        val lng2Part1 =
            sin(bearing) * sin(radiusFraction) * cos(
                lat1
            )
        val lng2Part2 =
            cos(radiusFraction) - sin(lat1) * sin(lat2)
        var lng2 = lng1 + atan2(lng2Part1, lng2Part2)
        lng2 = (lng2 + 3.0 * Math.PI) % (2.0 * Math.PI) - Math.PI
        return GeoPoint(
            Math.toDegrees(
                lat2
            ), Math.toDegrees(lng2)
        )
    }

    private fun pointPlusDistanceEast(point: GeoPoint, distance: Double): GeoPoint {
        return getPointAhead(point, distance, 90.0)
    }

    private fun pointPlusDistanceNorth(point: GeoPoint, distance: Double): GeoPoint {
        return getPointAhead(point, distance, 0.0)
    }

    fun calculateDistance(track: Array<GeoPoint>?): Double {
        var distance = 0.0
        var lastLon: Double
        var lastLat: Double
        //WARNING! I didn't find array.length type. Seems it's int, so we can use next comparison:
        if (track == null || track.size - 1 <= 0) //track.length == 0 || track.length == 1
            return 0.0
        lastLon = track[0].Longitude
        lastLat = track[0].Latitude
        for (i in 1 until track.size) {
            distance += distanceBetween(
                lastLat, lastLon,
                track[i].Latitude, track[i].Longitude
            )
            lastLat = track[i].Latitude
            lastLon = track[i].Longitude
        }
        return distance
    }
}