package com.apprikart.rotationmatrixdemo.loggers

import android.location.Location
import com.apprikart.rotationmatrixdemo.filters.GeoHash
import com.apprikart.rotationmatrixdemo.filters.GeoPoint

class GeohashRTFilter(geohashPrecision: Int, geohashminPointCount: Int) {

    private var mGeohashPrecision: Int = 0
    private var mGeohashMinPointCount: Int = 0
    private var isFirstCoordinate = true
    private lateinit var geoHashBuffers: LongArray
    private val ppCompGeoHash = 0
    private val ppReadGeoHash = 1
    private val currentGeoPoint: GeoPoint? = null
    private val lastApprovedGeoPoint: GeoPoint? = null
    private val lastGeoPointAsIs: GeoPoint? = null

    init {
        mGeohashPrecision = geohashPrecision
        mGeohashMinPointCount = geohashminPointCount

    }

    fun filter(loc: Location) {

        val pi = GeoPoint(loc.latitude, loc.longitude)

        /*if (isFirstCoordinate) {
            geoHashBuffers[ppCompGeoHash] =
                GeoHash.encode_u64(pi.latitude, pi.longitude, mGeohashPrecision)
        }*/

    }

}