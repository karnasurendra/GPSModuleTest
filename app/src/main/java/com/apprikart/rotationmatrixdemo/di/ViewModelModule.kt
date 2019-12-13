package com.apprikart.rotationmatrixdemo.di

import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.filters.GPSAccKalmanFilterNew
import com.apprikart.rotationmatrixdemo.loggers.GeohashRTFilter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    // Whatever we want to provide singleton class, we can write here like Retrofit, WebService.. with provides Annotation

    @Provides
    @Singleton
    fun getGpsAccKalmanFilter(): GPSAccKalmanFilterNew {
        return GPSAccKalmanFilterNew(true)
    }

    @Provides
    @Singleton
    fun getGeoHashRTFilter(): GeohashRTFilter {
        return GeohashRTFilter(Utils.GEOHASH_DEFAULT_PREC, Utils.GEOHASH_DEFAULT_MIN_POINT_COUNT)
    }


}