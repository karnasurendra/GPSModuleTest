package com.apprikart.rotationmatrixdemo.di

import android.app.Application
import com.apprikart.rotationmatrixdemo.SensorsApp
import dagger.Module
import dagger.Provides

@Module
class AppModule(private val sensorsApp: SensorsApp) {

    @Provides
    fun provideApplication(): Application {
        return sensorsApp
    }

}