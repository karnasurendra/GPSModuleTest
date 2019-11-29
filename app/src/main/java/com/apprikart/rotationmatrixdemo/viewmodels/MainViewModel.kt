package com.apprikart.rotationmatrixdemo.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.IllegalArgumentException
import javax.inject.Inject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    fun createTextFiles(dir: File, textFileName: String) {
        val file = File(dir.absolutePath, textFileName)
        if (!file.exists()) {
            file.createNewFile()
            writeToFileInitially(file)
        }
    }

    private fun writeToFileInitially(file: File) {
        val fileOutputStream = FileOutputStream(file, true)
        val outputStreamWriter = OutputStreamWriter(fileOutputStream)
        outputStreamWriter.append("[")
        outputStreamWriter.close()
        fileOutputStream.close()
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        class Factory @Inject constructor(var application: Application) :
            ViewModelProvider.Factory {

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    MainViewModel(application) as T
                } else {
                    throw IllegalArgumentException("ViewModel not found")
                }
            }

        }
    }

}