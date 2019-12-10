package com.apprikart.rotationmatrixdemo.loggers

import android.location.Location
import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.filters.Coordinates
import com.apprikart.rotationmatrixdemo.filters.GeoHash
import com.apprikart.rotationmatrixdemo.filters.GeoPoint
import com.elvishew.xlog.XLog

class GeohashRTFilter(private val geohashPrecision: Int, private val geohashminPointCount: Int) {

    private val coOrdNotInitialized = 361.0
    private val providerName = "GeoHashFiltered"
    private var isFirstCoordinate = true
    private lateinit var geoHashBuffers: LongArray
    private var ppCompGeoHash = 0
    private var ppReadGeoHash = 1
    private var pointsInCurrentGeoHashCount = 0
    private lateinit var currentGeoPoint: GeoPoint
    private lateinit var lastApprovedGeoPoint: GeoPoint
    private lateinit var lastGeoPointAsIs: GeoPoint
    private var mDistanceAsIs: Double = 0.0
    private var mDistanceAsIsHp: Double = 0.0
    private val hpResBuffAsIs = FloatArray(3)
    private val hpResBuffGeo = FloatArray(3)
    private var distanceGeoFiltered = 0.0
    private var distanceGeoFilteredHp = 0.0
    private var mGeoFilteredTrack = mutableListOf<Location>()

    init {
        reset()
    }

    private fun reset() {
        mGeoFilteredTrack.clear()
        geoHashBuffers = LongArray(2)
        pointsInCurrentGeoHashCount = 0
        lastApprovedGeoPoint = GeoPoint(coOrdNotInitialized, coOrdNotInitialized)
        currentGeoPoint = GeoPoint(coOrdNotInitialized, coOrdNotInitialized)
        lastGeoPointAsIs = GeoPoint(coOrdNotInitialized, coOrdNotInitialized)
        distanceGeoFiltered = 0.0.also { distanceGeoFilteredHp = it }
        mDistanceAsIs = 0.0.also { mDistanceAsIsHp = it }
        isFirstCoordinate = true
    }

    fun filter(loc: Location) {

        XLog.i("${Utils.GEOHASH_FILTERED_GPS_DATA} :: Time : ${loc.time}, Latitude : ${loc.latitude}, Longitude : ${loc.longitude}, Altitude : ${loc.altitude}")


        val pi = GeoPoint(loc.latitude, loc.longitude)

        // First time we are updating the current Geo point and last Geo point with the first location values
        if (isFirstCoordinate) {
            geoHashBuffers[ppCompGeoHash] =
                GeoHash.encodeU64(pi.latitude, pi.longitude, geohashPrecision)
            currentGeoPoint.latitude = pi.latitude
            currentGeoPoint.longitude = pi.longitude
            pointsInCurrentGeoHashCount = 1

            isFirstCoordinate = false
            lastGeoPointAsIs.latitude = pi.latitude
            lastGeoPointAsIs.longitude = pi.longitude
            return
        }

        mDistanceAsIs += Coordinates.distanceBetween(
            lastGeoPointAsIs.longitude,
            lastGeoPointAsIs.latitude,
            pi.longitude,
            pi.latitude
        )

        Location.distanceBetween(
            lastGeoPointAsIs.latitude,
            lastGeoPointAsIs.longitude,
            pi.latitude,
            pi.longitude,
            hpResBuffAsIs
        )

        mDistanceAsIsHp += hpResBuffAsIs[0]
        lastGeoPointAsIs.longitude = loc.longitude
        lastGeoPointAsIs.latitude = loc.latitude

        geoHashBuffers[ppReadGeoHash] =
            GeoHash.encodeU64(pi.latitude, pi.longitude, geohashPrecision)

        if (geoHashBuffers[ppCompGeoHash] != geoHashBuffers[ppReadGeoHash]) {

            if (pointsInCurrentGeoHashCount >= geohashminPointCount) {
                currentGeoPoint.latitude /= pointsInCurrentGeoHashCount
                currentGeoPoint.longitude /= pointsInCurrentGeoHashCount

                if (lastApprovedGeoPoint.latitude != coOrdNotInitialized) {
                    val dd1 = Coordinates.distanceBetween(
                        lastApprovedGeoPoint.longitude,
                        lastApprovedGeoPoint.latitude,
                        currentGeoPoint.longitude,
                        currentGeoPoint.latitude
                    )
                    distanceGeoFiltered += dd1
                    Location.distanceBetween(
                        lastApprovedGeoPoint.latitude,
                        lastApprovedGeoPoint.longitude,
                        currentGeoPoint.latitude,
                        currentGeoPoint.longitude,
                        hpResBuffGeo
                    )
                    val dd2 = hpResBuffGeo[0]
                    distanceGeoFilteredHp += dd2
                }
                lastApprovedGeoPoint.longitude = currentGeoPoint.longitude
                lastApprovedGeoPoint.latitude = currentGeoPoint.latitude
                val laLoc = Location(providerName)
                laLoc.latitude = lastApprovedGeoPoint.latitude
                laLoc.longitude = lastApprovedGeoPoint.longitude
                laLoc.altitude = loc.altitude
                laLoc.time = loc.time
                mGeoFilteredTrack.add(laLoc)
                currentGeoPoint.latitude = 0.0.also { currentGeoPoint.longitude = it }
            }

            pointsInCurrentGeoHashCount = 1
            currentGeoPoint.latitude = pi.latitude
            currentGeoPoint.longitude = pi.longitude
            // Swap buffers
            val swp = ppCompGeoHash
            ppCompGeoHash = ppReadGeoHash
            ppReadGeoHash = swp
            return
        }

        currentGeoPoint.latitude += pi.latitude
        currentGeoPoint.longitude += pi.longitude
        ++pointsInCurrentGeoHashCount
    }

    fun stop() {

        if (pointsInCurrentGeoHashCount >= geohashminPointCount) {

            currentGeoPoint.latitude /= pointsInCurrentGeoHashCount
            currentGeoPoint.longitude /= pointsInCurrentGeoHashCount

            if (lastApprovedGeoPoint.latitude != coOrdNotInitialized) {
                val dd1 = Coordinates.distanceBetween(
                    lastApprovedGeoPoint.longitude,
                    lastApprovedGeoPoint.latitude,
                    currentGeoPoint.longitude,
                    currentGeoPoint.latitude
                )
                distanceGeoFiltered += dd1
                Location.distanceBetween(
                    lastApprovedGeoPoint.latitude,
                    lastApprovedGeoPoint.longitude,
                    currentGeoPoint.latitude,
                    currentGeoPoint.longitude,
                    hpResBuffGeo
                )
                val dd2 = hpResBuffGeo[0]
                distanceGeoFilteredHp += dd2
            }

            lastApprovedGeoPoint.longitude = currentGeoPoint.longitude
            lastApprovedGeoPoint.latitude = currentGeoPoint.latitude

            val loc = Location(providerName)
            loc.latitude = lastApprovedGeoPoint.latitude
            loc.longitude = lastApprovedGeoPoint.longitude
            mGeoFilteredTrack.add(loc)
            currentGeoPoint.latitude = 0.0.also { currentGeoPoint.longitude = it }

        }

    }

    fun getDistanceGeoFiltered(): Double {
        return distanceGeoFiltered
    }

    fun getDistanceGeoFilteredHP(): Double {
        return distanceGeoFilteredHp
    }

    fun getDistanceAsIs(): Double {
        return mDistanceAsIs
    }

    fun getDistanceAsIsHP(): Double {
        return mDistanceAsIsHp
    }

    fun getGeoFilteredTrack(): List<Location> {
        return mGeoFilteredTrack
    }

}