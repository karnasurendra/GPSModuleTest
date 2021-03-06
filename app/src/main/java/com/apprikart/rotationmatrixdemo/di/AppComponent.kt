package com.apprikart.rotationmatrixdemo.di

import com.apprikart.rotationmatrixdemo.views.GPSActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent {

    // Where ever we are injecting the Dependency those classes has to include here like activities, fragments
    fun inject(mainActivity: GPSActivity)


}