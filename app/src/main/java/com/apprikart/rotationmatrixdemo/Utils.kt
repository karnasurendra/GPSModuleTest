package com.apprikart.rotationmatrixdemo

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.io.File

open class Utils {

    companion object {

        val ACCELERATION_TEXT_FILE: String = "/Acceleration.txt"
        val GYROSCOPE_TEXT_FILE: String = "/Gyroscope.txt"
        val MAGNETOMETER_TEXT_FILE: String = "/Magnetometer.txt"
        val LINEAR_ACCELERATION_TEXT_FILE: String = "/LinearAcceleraion.txt"
        val ROTATION_VECTOR_TEXT_FILE: String = "/RotationVector.txt"
        val LA_AFTER_ROTATION_TEXT_FILE: String = "/LAAfterRotation.txt"
        const val LOG_FILE = "/SensorsLogData"
        const val ACCELEROMETER_DEFAULT_DEVIATION = 0.1
        const val DEFAULT_VEL_FACTOR = 1.0
        const val DEFAULT_POS_FACTOR = 1.0
        const val ACCELERATION = "Accelerometer"
        const val GYROSCOPE = "Gyroscope"
        const val MAGNETOMETER = "Magnetometer"
        const val LINEAR_ACCELERATION = "Linear Acceleration"
        const val ROTATION_VECTOR = "Rotation Vector"
        const val LINEAR_ACCELERATION_AFTER_ROTATION = "Linear acceleration after rotation"

        fun nano2milli(nano: Long): Long {
            return (nano / 1e6).toLong()
        }

        fun hasPermissions(
            context: Context,
            permissions: Array<String>
        ): Boolean {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }

    }

}