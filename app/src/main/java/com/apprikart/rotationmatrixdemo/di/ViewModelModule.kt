package com.apprikart.rotationmatrixdemo.di

import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.filters.GPSAccKalmanFilter
import com.apprikart.rotationmatrixdemo.loggers.GeohashRTFilter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    // Whatever we want to provide as singleton we can write here like Retrofit, WebService.. with provides Annotation

    @Provides
    @Singleton
    fun getGpsAccKalmanFilter(): GPSAccKalmanFilter {
        return GPSAccKalmanFilter(
            false,
            0.0,
            0.0,
            0.0,
            0.0,
            Utils.ACCELEROMETER_DEFAULT_DEVIATION,
            0.0,
            0.0,
            Utils.DEFAULT_VEL_FACTOR,
            Utils.DEFAULT_POS_FACTOR,
            true
        )
    }

    @Provides
    @Singleton
    fun getGeoHashRTFilter(): GeohashRTFilter {
        return GeohashRTFilter(Utils.GEOHASH_DEFAULT_PREC, Utils.GEOHASH_DEFAULT_MIN_POINT_COUNT)
    }


}