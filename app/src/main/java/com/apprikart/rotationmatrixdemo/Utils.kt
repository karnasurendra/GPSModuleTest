package com.apprikart.rotationmatrixdemo

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

open class Utils {

    companion object {

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