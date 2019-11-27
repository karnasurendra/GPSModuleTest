package com.apprikart.rotationmatrixdemo

import android.app.Application
import com.apprikart.rotationmatrixdemo.di.AppComponent

class SensorsApp : Application() {


    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()


    }
}