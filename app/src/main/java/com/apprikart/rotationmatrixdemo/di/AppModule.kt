package com.apprikart.rotationmatrixdemo.di

import android.app.Application
import com.apprikart.rotationmatrixdemo.GPSApp
import dagger.Module
import dagger.Provides

@Module
class AppModule(private val GPSApp: GPSApp) {

    @Provides
    fun provideApplication(): Application {
        return GPSApp
    }

}