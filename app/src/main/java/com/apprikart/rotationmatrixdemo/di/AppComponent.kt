package com.apprikart.rotationmatrixdemo.di

import com.apprikart.rotationmatrixdemo.filters.GPSAccKalmanFilter
import com.apprikart.rotationmatrixdemo.views.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent {

    // Where ever we are injecting the Dependancy those classes has to include here like activities, fragments
    fun inject(mainActivity: MainActivity)


}