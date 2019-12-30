package com.apprikart.rotationmatrixdemo.filters

/**
 * Created by lezh1k on 2/13/18.
 */
object GeoHash {
    private fun interleave(x: Long, y: Long): Long {
        var x = x
        var y = y
        x = x or (x shl 16) and 0x0000ffff0000ffffL
        x = x or (x shl 8) and 0x00ff00ff00ff00ffL
        x = x or (x shl 4) and 0x0f0f0f0f0f0f0f0fL
        x = x or (x shl 2) and 0x3333333333333333L
        x = x or (x shl 1) and 0x5555555555555555L
        y = y or (y shl 16) and 0x0000ffff0000ffffL
        y = y or (y shl 8) and 0x00ff00ff00ff00ffL
        y = y or (y shl 4) and 0x0f0f0f0f0f0f0f0fL
        y = y or (y shl 2) and 0x3333333333333333L
        y = y or (y shl 1) and 0x5555555555555555L
        return x or (y shl 1)
        //use pdep instructions
//  return _pdep_u64(x, 0x5555555555555555) | _pdep_u64(y, 0xaaaaaaaaaaaaaaaa);
    }

    fun encodeU64(lat: Double, lon: Double, prec: Int): Long {
        var mLat = lat
        var mLong = lon
        mLat = mLat / 180.0 + 1.5
        mLong = mLong / 360.0 + 1.5
        var ilat = java.lang.Double.doubleToRawLongBits(mLat)
        var ilon = java.lang.Double.doubleToRawLongBits(mLong)
        ilat = ilat shr 20
        ilon = ilon shr 20
        ilat = ilat and 0x00000000ffffffffL
        ilon = ilon and 0x00000000ffffffffL
        return interleave(ilat, ilon) shr (GEOHASH_MAX_PRECISION - prec) * 5
    }

    val base32Table = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    )
    private const val GEOHASH_MAX_PRECISION = 12
    fun geohash_str(
        geohash: Long,
        prec: Int /*hack. we don't need it, but java hasn't unsigned values*/
    ): String {
        var geohash = geohash
        var prec = prec
        val buff = StringBuffer(GEOHASH_MAX_PRECISION)
        geohash =
            geohash shr 4 //cause we don't need last 4 bits. that's strange, I thought we don't need first 4 bits %)
        geohash = geohash and 0x0fffffffffffffffL //we don't need sign here
        while (prec-- > 0) {
            buff.append(base32Table[(geohash and 0x1f).toInt()])
            geohash = geohash shr 5
        }
        return buff.reverse().toString()
    }
}