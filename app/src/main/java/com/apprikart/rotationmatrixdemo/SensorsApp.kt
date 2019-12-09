package com.apprikart.rotationmatrixdemo

import android.app.Application
import com.apprikart.rotationmatrixdemo.di.AppComponent
import com.apprikart.rotationmatrixdemo.di.AppModule
import com.apprikart.rotationmatrixdemo.di.DaggerAppComponent
import com.apprikart.rotationmatrixdemo.di.ViewModelModule
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.DefaultFlattener
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import com.elvishew.xlog.printer.file.naming.FileNameGenerator
import java.io.File

class SensorsApp : Application() {

    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .viewModelModule(ViewModelModule())
            .build()

        initXLog()


    }

    private fun initXLog() {

        val printer = FilePrinter
            .Builder("${File(getExternalFilesDir(null), Utils.LOG_FOLDER)}")
            .fileNameGenerator(DateFileNameGenerator())
            .backupStrategy(NeverBackupStrategy())
            .cleanStrategy(FileLastModifiedCleanStrategy(Long.MAX_VALUE))
            .flattener(DefaultFlattener()).build()

        XLog.init(LogLevel.ALL, printer)

    }

    fun getComponent(): AppComponent {
        return appComponent
    }


}