package com.apprikart.rotationmatrixdemo

import android.app.Application
import com.apprikart.rotationmatrixdemo.di.AppComponent
import com.apprikart.rotationmatrixdemo.di.AppModule
import com.apprikart.rotationmatrixdemo.di.DaggerAppComponent
import com.apprikart.rotationmatrixdemo.di.ViewModelModule

open class SensorsApp : Application() {

    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .viewModelModule(ViewModelModule())
            .build()
    }

    fun getComponent(): AppComponent {
        return appComponent
    }


}