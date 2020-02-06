package com.apprikart.rotationmatrixdemo.views

import android.Manifest
import android.content.Context
import android.hardware.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.apprikart.rotationmatrixdemo.SensorsApp
import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.filters.Coordinates
import com.apprikart.rotationmatrixdemo.models.SensorGpsDataItem
import com.apprikart.rotationmatrixdemo.viewmodels.MainViewModel
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

abstract class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private lateinit var sensorManager: SensorManager
    private val mRotationMatrix = FloatArray(16)
    private val invertedRotationMatrix = FloatArray(16)
    private val linAcceleration = FloatArray(4)
    private val accelerationVector = FloatArray(4)
    private lateinit var sensorEventListener: SensorEventListener
    /**
     *  This is the declination of the horizontal component of the magnetic field from true north, in degrees
     *  Ref link : https://www.youtube.com/watch?v=uN5w24F4hGk
     * Magnetic declination can be calculated using Location latitude, longitude and Altitude
     */
    private var mMagneticDeclination: Double = 0.0

    // SensorDataItem data will be added to this Queue
    private val mSensorDataQueue: Queue<SensorGpsDataItem> =
        PriorityBlockingQueue()
    private lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var mainViewModelFactory: MainViewModel.Companion.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initializing the Dependency injection
        (application as SensorsApp).getComponent().inject(this)
        // Initializing ViewModel with view model factory
        mainViewModel =
            ViewModelProviders.of(this, mainViewModelFactory).get(MainViewModel::class.java)

        // Location values will observe Here
        mainViewModel.location.observe(this, androidx.lifecycle.Observer {
            onLocationUpdate(it)
        })
    }

    override fun onResume() {
        super.onResume()

        sensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                // Nothing has to do
            }

            override fun onSensorChanged(event: SensorEvent) {
                // Actually we will get 4 x 1 vector after multiplying inverse rotation Matrix with Linear acceleration values, 0th pos is east, 1st pos is North and 2nd pos is Up
                val east = 0
                val north = 1
                val up = 2

                val now = android.os.SystemClock.elapsedRealtimeNanos()
                val nowMs =
                    Utils.nano2milli(now)

                when (event.sensor.type) {
                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        // Converting the Linear Acceleration values to an Array
                        System.arraycopy(event.values, 0, linAcceleration, 0, event.values.size)
                        /**Multiplying the inverted Rotation Matrix values with the linear acceleration sensor values
                        Getting acceleration vector in the “absolute” coordinate system from invertedRotationMatrix and Linear Acceleration values
                         */
                        android.opengl.Matrix.multiplyMV(
                            accelerationVector,
                            0,
                            invertedRotationMatrix,
                            0,
                            linAcceleration,
                            0
                        )

                        // It will initialize once the Location details get triggered
                        if (mainViewModel.gpsAccKalmanFilter.isInitializedFromDI()) {
                            return
                        }

                        // Creating the SensorDataItem object and Location values not available here so which are the fields are not available made them as Not Initialized
                        val sensorGpsDataItem =
                            SensorGpsDataItem(
                                nowMs.toDouble(),
                                SensorGpsDataItem.NOT_INITIALIZED,
                                SensorGpsDataItem.NOT_INITIALIZED,
                                SensorGpsDataItem.NOT_INITIALIZED,
                                accelerationVector[north].toDouble(),
                                accelerationVector[east].toDouble(),
                                accelerationVector[up].toDouble(),
                                SensorGpsDataItem.NOT_INITIALIZED,
                                SensorGpsDataItem.NOT_INITIALIZED,
                                SensorGpsDataItem.NOT_INITIALIZED,
                                SensorGpsDataItem.NOT_INITIALIZED,
                                mMagneticDeclination
                            )

                        // Adding the sensor data item object to Queue
                        mSensorDataQueue.add(sensorGpsDataItem)

                    }

                    Sensor.TYPE_ROTATION_VECTOR -> {
                        /** Getting Rotation Matrix values from Rotation Vector Component,
                        which is 16 size array in Matrix form 4 x 4 matrix
                         *  one dimensions for each axis x, y, and z, plus one dimension to represent the
                        “origin” in the coordinate system. These are known as homogeneous coordinates*/
                        SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values)

                        // Inverting the 4 x 4 Rotation Matrix Values and saving to invertedRotationMatrix
                        android.opengl.Matrix.invertM(invertedRotationMatrix, 0, mRotationMatrix, 0)
                    }
                }
            }

        }
    }

    /** This is the method which will start the Sensor
     * */
    fun startGps() {
        if (isAllSensorsAvailable()) {
            sensorsAvailability(true)
            checkPermissions()
        } else {
            sensorsAvailability(false)
            return
        }

        // Updating the distance in Text View - Optional
        mainViewModel.geoValues.observeForever {
            //            mBinding.distanceValuesTv.text = it
        }

    }

    /**Disconnection Part
     * Whenever want to stop the GPS Module, unregistering the Sensors, stopping the location filter and removing the Location updates
     * This can be called when the app goes to onPause or onStop State*/
    fun stopGps() {
        unRegisterSensors()
        // Stopping the filter
        mainViewModel.geohashRTFilter.stop()
        mainViewModel.removeLocation()
    }

    /**This method will be useful when the application comes to restart state*/
    fun reStartGps() {
        initializeSensors()
        mainViewModel.geohashRTFilter.reset()
        mainViewModel.initLocation()
    }

    /**This method has to override in the Child class to get the to get the permission details issue*/
    abstract fun permissionIssue(permissions: Array<String>)

    /**This has to override in the Child class to get the sensors availability*/
    abstract fun sensorsAvailability(isSensorsAvailable: Boolean)

    private fun onLocationUpdate(location: Location) {

        if (location.accuracy > 15) return

        val xLong: Double = location.longitude
        val yLat: Double = location.latitude
        val speed: Double = location.speed.toDouble()
        /**Bearing is the horizontal direction of travel of this device, and is not related to the device orientation.
         *It is guaranteed to be in the range (0.0, 360.0] if the device has a bearing.
         *If this location does not have a bearing then 0.0 is returned.*/
        val course: Double = location.bearing.toDouble()
        val xVel: Double = speed * cos(course)
        val yVel: Double = speed * sin(course)
        val accuracy: Double = location.accuracy.toDouble()

        val timeStamp: Long =
            Utils.nano2milli(
                location.elapsedRealtimeNanos
            )
        /**WARNING!!! here should be speed accuracy, but loc.hasSpeedAccuracy()
         *and loc.getSpeedAccuracyMetersPerSecond() requires API 26*/
        val velError = location.accuracy * 0.1

        updateMagneticDeclination(location, timeStamp)

        // Only once it has to initialize, It will initialize from DI it will not be null
        if (mainViewModel.gpsAccKalmanFilter.isInitializedFromDI()) {
            mainViewModel.gpsAccKalmanFilter.manualInit(
                false, // As per the reference project ( Mad-location-manager-master ) it is always false
                Coordinates.longitudeToMeters(xLong),
                Coordinates.latitudeToMeters(yLat),
                xVel,
                yVel,
                Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                accuracy,
                timeStamp.toDouble(),
                Utils.DEFAULT_VEL_FACTOR,
                Utils.DEFAULT_POS_FACTOR,
                false
            )
            return
        }

        val sensorGpsDataItem =
            SensorGpsDataItem(
                timeStamp.toDouble(),
                location.latitude,
                location.longitude,
                location.altitude,
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                location.speed.toDouble(),
                location.bearing.toDouble(),
                location.accuracy.toDouble(),
                velError,
                mMagneticDeclination
            )

        mSensorDataQueue.add(sensorGpsDataItem)

    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Utils.hasPermissions(
                    this,
                    permissions
                )
            ) {
                init()
            } else {
                permissionIssue(permissions)
            }
        } else {
            init()
        }
    }


    private fun init() {
        /*Below method is to create directory and file in the Storage*/
//        createDirAndFile()
        // Location implementation done by using reference of Map Box code
        mainViewModel.initLocation()
        // Initializing the Sensors
        initializeSensors()
        mainViewModel.needTerminate = false
        // Starting the background task
        mainViewModel.initSensorDataLoopTask(mSensorDataQueue)
    }

    private fun initializeSensors() {
        // Initializing System Service
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        /**Registering the Linear Acceleration Sensor*/
        registerLA()
        /**Registering the Rotation Vector Sensor*/
        registerRotationVector()
    }

    private fun isAllSensorsAvailable(): Boolean {
        return (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null && sensorManager.getDefaultSensor(
            Sensor.TYPE_ROTATION_VECTOR
        ) != null)
    }

    /**Registering the LA(Linear Acceleration) Sensor*/
    private fun registerLA() {
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    /**Registering the Rotation Vector Sensor*/
    private fun registerRotationVector() {
        sensorManager.registerListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }


    /**Getting the Magnetic declination value of the particular location*/
    private fun updateMagneticDeclination(
        location: Location,
        timeStamp: Long
    ) {
        val geomagneticField =
            GeomagneticField(
                location.latitude.toFloat(),
                location.longitude.toFloat(), location.altitude.toFloat(), timeStamp
            )
        mMagneticDeclination = geomagneticField.declination.toDouble()
    }

    private fun unRegisterSensors() {
        sensorManager.unregisterListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        )
        sensorManager.unregisterListener(
            sensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        )
    }

}
