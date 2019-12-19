package com.apprikart.rotationmatrixdemo

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

open class Utils {

    companion object {
        const val LOCATION_FROM_FILTER = "Location Service"
        const val GEOHASH_DEFAULT_PREC = 6
//        const val GEOHASH_DEFAULT_PREC = 7
        const val GEOHASH_DEFAULT_MIN_POINT_COUNT = 2
        const val ACCELERATION_TEXT_FILE = "/Acceleration.txt"
        const val GYROSCOPE_TEXT_FILE = "/Gyroscope.txt"
        const val MAGNETOMETER_TEXT_FILE = "/Magnetometer.txt"
        const val LINEAR_ACCELERATION_TEXT_FILE = "/LinearAcceleraion.txt"
        const val ROTATION_VECTOR_TEXT_FILE = "/RotationVector.txt"
        const val LA_AFTER_ROTATION_TEXT_FILE = "/LAAfterRotation.txt"
        const val LOG_FOLDER = "SensorsLogs"
        const val ACCELEROMETER_DEFAULT_DEVIATION = 0.1
        const val DEFAULT_VEL_FACTOR = 1.0
        const val DEFAULT_POS_FACTOR = 1.0
        const val ACCELERATION = "Accelerometer"
        const val GYROSCOPE = "Gyroscope"
        const val MAGNETOMETER = "Magnetometer"
        const val LINEAR_ACCELERATION = "Linear Acceleration"
        const val ROTATION_VECTOR = "Rotation Vector"
        const val ROTATION_MATRIX = "Rotation Matrix"
        const val INVERSE_ROTATION_MATRIX = "Inverse Rotation Matrix"
        const val ACCELERATION_IN_ABSOLUTE_COORDINATE_SYSTEM =
            "Acceleration vector in absolute coordinate system"
        const val LINEAR_ACCELERATION_AFTER_ROTATION = "Linear acceleration after rotation"
        const val KALMAN_FILTER_PREDICTED_STATE = "Kalman filter predicted state estimate"
        const val KALMAN_FILTER_PREDICTED_STATE_LOCATION = "Kalman filter predicted Lat Long Values"
        const val GEOHASH_FILTERED_GPS_DATA = "Filtered GPS Data"
        const val LOCATION_UPDATED_DATA = "Location from GPS"
        const val KALMAN_FILTER_UPDATED_STATE = "Kalman filter updated state estimate"
        const val KALMAN_FILTER_UPDATED_STATE_LOCATION = "Kalman filter updated Location"
        const val KALMAN_FILTER_PREDICTED_ESTIMATE_COVARIANCE =
            "Kalman filter predicted estimate covariance"
        const val KALMAN_FILTER_UPDATED_ESTIMATE_COVARIANCE =
            "Kalman filter updated estimate covariance"

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