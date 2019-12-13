package com.apprikart.rotationmatrixdemo.loggers

import android.location.Location
import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.filters.CoordinatesNew
import com.apprikart.rotationmatrixdemo.filters.GeoHashNew
import com.apprikart.rotationmatrixdemo.filters.GeoPointNew
import com.elvishew.xlog.XLog

class GeohashRTFilter(private val geohashPrecision: Int, private val geohashminPointCount: Int) {

    private val coOrdNotInitialized = 361.0
    private val providerName = "GeoHashFiltered"
    private var isFirstCoordinate = true
    private lateinit var geoHashBuffers: LongArray
    private var ppCompGeoHash = 0
    private var ppReadGeoHash = 1
    private var pointsInCurrentGeoHashCount = 0
    private lateinit var currentGeoPointNew: GeoPointNew
    private lateinit var lastApprovedGeoPointNew: GeoPointNew
    private lateinit var lastGeoPointNewAsIs: GeoPointNew
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
        lastApprovedGeoPointNew = GeoPointNew(coOrdNotInitialized, coOrdNotInitialized)
        currentGeoPointNew = GeoPointNew(coOrdNotInitialized, coOrdNotInitialized)
        lastGeoPointNewAsIs = GeoPointNew(coOrdNotInitialized, coOrdNotInitialized)
        distanceGeoFiltered = 0.0.also { distanceGeoFilteredHp = it }
        mDistanceAsIs = 0.0.also { mDistanceAsIsHp = it }
        isFirstCoordinate = true
    }

    fun filter(loc: Location) {

        XLog.i("${Utils.GEOHASH_FILTERED_GPS_DATA} :: Time : ${loc.time}, Latitude : ${loc.latitude}, Longitude : ${loc.longitude}, Altitude : ${loc.altitude}")


        val pi = GeoPointNew(loc.latitude, loc.longitude)

        // First time we are updating the current Geo point and last Geo point with the first location values
        if (isFirstCoordinate) {
            geoHashBuffers[ppCompGeoHash] =
                GeoHashNew.encodeU64(pi.latitude, pi.longitude, geohashPrecision)
            currentGeoPointNew.latitude = pi.latitude
            currentGeoPointNew.longitude = pi.longitude
            pointsInCurrentGeoHashCount = 1

            isFirstCoordinate = false
            lastGeoPointNewAsIs.latitude = pi.latitude
            lastGeoPointNewAsIs.longitude = pi.longitude
            return
        }

        mDistanceAsIs += CoordinatesNew.distanceBetween(
            lastGeoPointNewAsIs.longitude,
            lastGeoPointNewAsIs.latitude,
            pi.longitude,
            pi.latitude
        )

        Location.distanceBetween(
            lastGeoPointNewAsIs.latitude,
            lastGeoPointNewAsIs.longitude,
            pi.latitude,
            pi.longitude,
            hpResBuffAsIs
        )

        mDistanceAsIsHp += hpResBuffAsIs[0]
        lastGeoPointNewAsIs.longitude = loc.longitude
        lastGeoPointNewAsIs.latitude = loc.latitude

        geoHashBuffers[ppReadGeoHash] =
            GeoHashNew.encodeU64(pi.latitude, pi.longitude, geohashPrecision)

        if (geoHashBuffers[ppCompGeoHash] != geoHashBuffers[ppReadGeoHash]) {

            if (pointsInCurrentGeoHashCount >= geohashminPointCount) {
                currentGeoPointNew.latitude /= pointsInCurrentGeoHashCount
                currentGeoPointNew.longitude /= pointsInCurrentGeoHashCount

                if (lastApprovedGeoPointNew.latitude != coOrdNotInitialized) {
                    val dd1 = CoordinatesNew.distanceBetween(
                        lastApprovedGeoPointNew.longitude,
                        lastApprovedGeoPointNew.latitude,
                        currentGeoPointNew.longitude,
                        currentGeoPointNew.latitude
                    )
                    distanceGeoFiltered += dd1
                    Location.distanceBetween(
                        lastApprovedGeoPointNew.latitude,
                        lastApprovedGeoPointNew.longitude,
                        currentGeoPointNew.latitude,
                        currentGeoPointNew.longitude,
                        hpResBuffGeo
                    )
                    val dd2 = hpResBuffGeo[0]
                    distanceGeoFilteredHp += dd2
                }
                lastApprovedGeoPointNew.longitude = currentGeoPointNew.longitude
                lastApprovedGeoPointNew.latitude = currentGeoPointNew.latitude
                val laLoc = Location(providerName)
                laLoc.latitude = lastApprovedGeoPointNew.latitude
                laLoc.longitude = lastApprovedGeoPointNew.longitude
                laLoc.altitude = loc.altitude
                laLoc.time = loc.time
                mGeoFilteredTrack.add(laLoc)
                currentGeoPointNew.latitude = 0.0.also { currentGeoPointNew.longitude = it }
            }

            pointsInCurrentGeoHashCount = 1
            currentGeoPointNew.latitude = pi.latitude
            currentGeoPointNew.longitude = pi.longitude
            // Swap buffers
            val swp = ppCompGeoHash
            ppCompGeoHash = ppReadGeoHash
            ppReadGeoHash = swp
            return
        }

        currentGeoPointNew.latitude += pi.latitude
        currentGeoPointNew.longitude += pi.longitude
        ++pointsInCurrentGeoHashCount
    }

    fun stop() {

        if (pointsInCurrentGeoHashCount >= geohashminPointCount) {

            currentGeoPointNew.latitude /= pointsInCurrentGeoHashCount
            currentGeoPointNew.longitude /= pointsInCurrentGeoHashCount

            if (lastApprovedGeoPointNew.latitude != coOrdNotInitialized) {
                val dd1 = CoordinatesNew.distanceBetween(
                    lastApprovedGeoPointNew.longitude,
                    lastApprovedGeoPointNew.latitude,
                    currentGeoPointNew.longitude,
                    currentGeoPointNew.latitude
                )
                distanceGeoFiltered += dd1
                Location.distanceBetween(
                    lastApprovedGeoPointNew.latitude,
                    lastApprovedGeoPointNew.longitude,
                    currentGeoPointNew.latitude,
                    currentGeoPointNew.longitude,
                    hpResBuffGeo
                )
                val dd2 = hpResBuffGeo[0]
                distanceGeoFilteredHp += dd2
            }

            lastApprovedGeoPointNew.longitude = currentGeoPointNew.longitude
            lastApprovedGeoPointNew.latitude = currentGeoPointNew.latitude

            val loc = Location(providerName)
            loc.latitude = lastApprovedGeoPointNew.latitude
            loc.longitude = lastApprovedGeoPointNew.longitude
            mGeoFilteredTrack.add(loc)
            currentGeoPointNew.latitude = 0.0.also { currentGeoPointNew.longitude = it }

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