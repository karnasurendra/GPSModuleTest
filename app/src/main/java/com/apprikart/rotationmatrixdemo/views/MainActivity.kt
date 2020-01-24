package com.apprikart.rotationmatrixdemo.views

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.apprikart.rotationmatrixdemo.R
import com.apprikart.rotationmatrixdemo.SensorsApp
import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.databinding.ActivityMainBinding
import com.apprikart.rotationmatrixdemo.filters.Coordinates
import com.apprikart.rotationmatrixdemo.models.SensorGpsDataItem
import com.apprikart.rotationmatrixdemo.viewmodels.MainViewModel
import com.elvishew.xlog.XLog
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity(), SensorEventListener {

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //Nothing has to do
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
            Sensor.TYPE_ACCELEROMETER -> {
                for (i in event.values.indices) {
                    when (i) {
                        0 -> mBinding.accXVal.text = event.values[i].toString()
                        1 -> mBinding.accYVal.text = event.values[i].toString()
                        2 -> mBinding.accZVal.text = event.values[i].toString()
                    }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                for (i in event.values.indices) {
                    when (i) {
                        0 -> mBinding.gyroXVal.text = event.values[i].toString()
                        1 -> mBinding.gyroYVal.text = event.values[i].toString()
                        2 -> mBinding.gyroZVal.text = event.values[i].toString()
                    }
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                for (i in event.values.indices) {
                    when (i) {
                        0 -> mBinding.magXVal.text = event.values[i].toString()
                        1 -> mBinding.magYVal.text = event.values[i].toString()
                        2 -> mBinding.magZVal.text = event.values[i].toString()
                    }
                }
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                for (i in event.values.indices) {
                    when (i) {
                        0 -> mBinding.linearXVal.text = event.values[i].toString()
                        1 -> mBinding.linearYVal.text = event.values[i].toString()
                        2 -> mBinding.linearZVal.text = event.values[i].toString()
                    }
                }

                // Converting the event values to an Array
                System.arraycopy(event.values, 0, linAcceleration, 0, event.values.size)

//                XLog.i("${Utils.LINEAR_ACCELERATION} - \n [ ${linAcceleration[0]} \n ${linAcceleration[1]} \n ${linAcceleration[2]} \n ${linAcceleration[3]} ]")

                /*XLog.i(
                    "${Utils.INVERSE_ROTATION_MATRIX} From Linear Acceleration - \n [" +
                            "${invertedRotationMatrix[0]} ${invertedRotationMatrix[1]} ${invertedRotationMatrix[2]} ${invertedRotationMatrix[3]} \n" +
                            "${invertedRotationMatrix[4]} ${invertedRotationMatrix[5]} ${invertedRotationMatrix[6]} ${invertedRotationMatrix[7]} \n" +
                            "${invertedRotationMatrix[8]} ${invertedRotationMatrix[9]} ${invertedRotationMatrix[10]} ${invertedRotationMatrix[11]} \n" +
                            "${invertedRotationMatrix[12]} ${invertedRotationMatrix[13]} ${invertedRotationMatrix[14]} ${invertedRotationMatrix[15]}  ]"
                )*/

                // Multiplying the inverted Rotation Matrix values with the linear acceleration sensor values
                android.opengl.Matrix.multiplyMV(
                    accelerationVector,
                    0,
                    invertedRotationMatrix,
                    0,
                    linAcceleration,
                    0
                )

//                XLog.i("${Utils.ACCELERATION_IN_ABSOLUTE_COORDINATE_SYSTEM} - \n [ ${accelerationVector[0]} \n ${accelerationVector[1]} \n ${accelerationVector[2]} \n ${accelerationVector[3]} ]")

                // acceleration vector in the “absolute” coordinate system
                rotation_matrix_x_val.text = accelerationVector[0].toString()
                rotation_matrix_y_val.text = accelerationVector[1].toString()
                rotation_matrix_z_val.text = accelerationVector[2].toString()

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

                // We are checking whether the parallel thread is running or not, if it is not running we are starting again
                /*if (!mainViewModel.isTaskLooping) {
                    mainViewModel.initSensorDataLoopTask(mSensorDataQueueNew)
                }*/

            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                for (i in event.values.indices) {
                    when (i) {
                        0 -> mBinding.rotationVectorXVal.text = event.values[i].toString()
                        1 -> mBinding.rotationVectorYVal.text = event.values[i].toString()
                        2 -> mBinding.rotationVectorZVal.text = event.values[i].toString()
                    }
                }

                /*XLog.i(
                    "${Utils.ROTATION_VECTOR} - \n [" +
                            "${event.values[0]} \n ${event.values[1]} \n ${event.values[2]} \n ${event.values[3]} \n ]"
                )*/


                // Getting Rotation Matrix values from Rotation Vector Component, which is 16 size array in Matrix form 4 x 4 matrix
                // one dimensions for each axis x, y, and z, plus one dimension to represent the “origin” in the coordinate system. These are known as homogenous coordinates
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values)


                /*XLog.d(
                    "${Utils.ROTATION_MATRIX} - \n [" +
                            "${mRotationMatrix[0]} ${mRotationMatrix[1]} ${mRotationMatrix[2]} ${mRotationMatrix[3]} \n" +
                            "${mRotationMatrix[4]} ${mRotationMatrix[5]} ${mRotationMatrix[6]} ${mRotationMatrix[7]} \n" +
                            "${mRotationMatrix[8]} ${mRotationMatrix[9]} ${mRotationMatrix[10]} ${mRotationMatrix[11]} \n" +
                            "${mRotationMatrix[12]} ${mRotationMatrix[13]} ${mRotationMatrix[14]} ${mRotationMatrix[15]}  ]"
                )*/

                // Inverting the 4 x 4 Rotation Matrix Values and saving to invertedRotationMatrix
                android.opengl.Matrix.invertM(invertedRotationMatrix, 0, mRotationMatrix, 0)

                /*XLog.d(
                    "${Utils.INVERSE_ROTATION_MATRIX} From Rotation Vector - \n [" +
                            "${invertedRotationMatrix[0]} ${invertedRotationMatrix[1]} ${invertedRotationMatrix[2]} ${invertedRotationMatrix[3]} \n" +
                            "${invertedRotationMatrix[4]} ${invertedRotationMatrix[5]} ${invertedRotationMatrix[6]} ${invertedRotationMatrix[7]} \n" +
                            "${invertedRotationMatrix[8]} ${invertedRotationMatrix[9]} ${invertedRotationMatrix[10]} ${invertedRotationMatrix[11]} \n" +
                            "${invertedRotationMatrix[12]} ${invertedRotationMatrix[13]} ${invertedRotationMatrix[14]} ${invertedRotationMatrix[15]}  ]"
                )*/
            }
        }
    }

    private var permissions = arrayOf(
//        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private lateinit var sensorManager: SensorManager
    private val mRotationMatrix = FloatArray(16)
    private val invertedRotationMatrix = FloatArray(16)
    private val linAcceleration = FloatArray(4)
    private val accelerationVector = FloatArray(4)
    /**
     *  This is the declination of the horizontal component of the magnetic field from true north, in degrees
     *  Ref link : https://www.youtube.com/watch?v=uN5w24F4hGk
     * Magnetic declination can be calculated using Location latitude, longitude and Altitude
     */
    private var mMagneticDeclination: Double = 0.0

    // SensorDataItem will be added to this Queue
    private val mSensorDataQueue: Queue<SensorGpsDataItem> =
        PriorityBlockingQueue()
    private val permissionReqCode = 100
    private val frequencyArrays = arrayOf("Normal", "Game", "UI", "Fast")
    private var isSensorAvailability = false
    private lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var mainViewModelFactory: MainViewModel.Companion.Factory
    private lateinit var mBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        (application as SensorsApp).getComponent().inject(this)

        // Initializing ViewModel with view model factory
        mainViewModel =
            ViewModelProviders.of(this, mainViewModelFactory).get(MainViewModel::class.java)

        // Mandatory to link between the ViewModel and XML
//        mBinding.lifecycleOwner = this
        mBinding.mainViewModel = mainViewModel

        // This will make screen awake
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Location values will observe Here
        mainViewModel.location.observe(this, androidx.lifecycle.Observer {
            onLocationUpdate(it)
        })

        checkPermissions()

        // Updating the distance in Text View
        mainViewModel.geoValues.observeForever {
            mBinding.distanceValuesTv.text = it
        }

    }

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


        XLog.d(
            "Location Data Latitude : ${location.latitude}, Longitude : ${location.longitude}, " +
                    "Speed : ${location.speed} Altitude : ${location.altitude} Time : ${location.time} Accuracy : ${location.accuracy}"
        )

        val timeStamp: Long =
            Utils.nano2milli(
                location.elapsedRealtimeNanos
            )
        /**WARNING!!! here should be speed accuracy, but loc.hasSpeedAccuracy()
         *and loc.getSpeedAccuracyMetersPerSecond() reqiares API 26*/
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
                ActivityCompat.requestPermissions(this, permissions, permissionReqCode)
            }
        } else {
            init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionReqCode) {
            for (permission in permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    //denied
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setMessage("Please provide permissions to continue ?")
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes) { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                            checkPermissions()
                        }
                        .setNegativeButton(R.string.no) { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                            finish()
                        }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                    break
                } else {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // All permissions allowed
                        if (permission == permissions[permissions.size - 1]) {
                            if (Utils.hasPermissions(
                                    this,
                                    this.permissions
                                )
                            ) {
                                init()
                            }
                        }
                    } else {
                        //set to never ask again
                        val alertDialogBuilder = AlertDialog.Builder(this)
                        alertDialogBuilder.setMessage("App requires permissions to continue. Allow permissions through settings.")
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                                dialogInterface.dismiss()
                                openAppSettings()
                            }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                        break
                    }
                }
            }
        }
    }

    private fun openAppSettings() {
        val packageUri: Uri = Uri.fromParts("package", application.packageName, null)
        val applicationIntent = Intent()
        applicationIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        applicationIntent.data = packageUri
        applicationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(applicationIntent)
    }

    private fun init() {
        // Location implementation done by using Map Box code implementation
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

        // Checking for Accelerometer and registering
        registerAccelerometer()

        // Checking for Gyroscope and registering
        registerGyroscope()

        // Checking for Magnetometer and registering
        registerMagnetometer()

        // Checking for Linear Acceleration and registering
        registerLinearAcceleration()

        // Checking for Rotation Vector and registering
        registerRotationVector()


    }


    private fun registerRotationVector() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            isSensorAvailability = true
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL
            )
            val arrayAdapter =
                ArrayAdapter<String>(
                    this,
                    R.layout.support_simple_spinner_dropdown_item,
                    frequencyArrays
                )
            rotation_vec_spinner.adapter = arrayAdapter
            rotation_vec_spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                        if (p0 != null) {
                            // First we need to unregister, to change the frequency
                            if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
                                sensorManager.unregisterListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                                )
                            }

                            when (p0.getItemAtPosition(p2).toString()) {
                                resources.getString(R.string.frequency_normal) -> {
                                    sensorManager.registerListener(
                                        this@MainActivity,
                                        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                                        SensorManager.SENSOR_DELAY_NORMAL
                                    )
                                }
                                resources.getString(R.string.frequency_game) -> {
                                    sensorManager.registerListener(
                                        this@MainActivity,
                                        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                                        SensorManager.SENSOR_DELAY_GAME
                                    )
                                }
                                resources.getString(R.string.frequency_ui) -> {
                                    sensorManager.registerListener(
                                        this@MainActivity,
                                        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                                        SensorManager.SENSOR_DELAY_UI
                                    )
                                }
                                resources.getString(R.string.frequency_fast) -> {
                                    sensorManager.registerListener(
                                        this@MainActivity,
                                        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                                        SensorManager.SENSOR_DELAY_FASTEST
                                    )
                                }
                            }
                        }
                    }
                }
        } else {
            val rotVecNa = resources.getString(R.string.rotation_vector_header) + " - NA"
            rotation_matrix_header.text = rotVecNa
            rotation_vec_spinner.visibility = GONE
            rotation_vector_x_val.text = resources.getString(R.string.na)
            rotation_vector_y_val.text = resources.getString(R.string.na)
            rotation_vector_z_val.text = resources.getString(R.string.na)
        }
    }

    private fun registerLinearAcceleration() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            isSensorAvailability = true
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_NORMAL
            )

            val arrayAdapter =
                ArrayAdapter<String>(
                    this,
                    R.layout.support_simple_spinner_dropdown_item,
                    frequencyArrays
                )
            linear_acc_spinner.adapter = arrayAdapter
            linear_acc_spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                        if (p0 != null) {

                            if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
                                sensorManager.unregisterListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                                )
                            }

                            when (p0.getItemAtPosition(p2).toString()) {
                                resources.getString(R.string.frequency_normal) -> {
                                    sensorManager.registerListener(
                                        this@MainActivity,
                                        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                                        SensorManager.SENSOR_DELAY_NORMAL
                                    )
                                }
                                resources.getString(R.string.frequency_game) -> {
                                    sensorManager.registerListener(
                                        this@MainActivity,
                                        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                                        SensorManager.SENSOR_DELAY_GAME
                                    )
                                }
                                resources.getString(R.string.frequency_ui) -> {
                                    sensorManager.registerListener(
                                        this@MainActivity,
                                        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                                        SensorManager.SENSOR_DELAY_UI
                                    )
                                }
                                resources.getString(R.string.frequency_fast) -> {
                                    sensorManager.registerListener(
                                        this@MainActivity,
                                        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                                        SensorManager.SENSOR_DELAY_FASTEST
                                    )
                                }
                            }
                        }
                    }
                }
        } else {
            val linAccNa = resources.getString(R.string.linear_acceleration) + " - NA"
            linear_acc_header.text = linAccNa
            linear_acc_spinner.visibility = GONE
            linear_x_val.text = resources.getString(R.string.na)
            linear_y_val.text = resources.getString(R.string.na)
            linear_z_val.text = resources.getString(R.string.na)
        }
    }

    private fun registerMagnetometer() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            isSensorAvailability = true
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL
            )

            val arrayAdapter =
                ArrayAdapter<String>(
                    this,
                    R.layout.support_simple_spinner_dropdown_item,
                    frequencyArrays
                )
            mag_spinner.adapter = arrayAdapter
            mag_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    if (p0 != null) {

                        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                            sensorManager.unregisterListener(
                                this@MainActivity,
                                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                            )
                        }

                        when (p0.getItemAtPosition(p2).toString()) {
                            resources.getString(R.string.frequency_normal) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                                    SensorManager.SENSOR_DELAY_NORMAL
                                )
                            }
                            resources.getString(R.string.frequency_game) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                                    SensorManager.SENSOR_DELAY_GAME
                                )
                            }
                            resources.getString(R.string.frequency_ui) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                                    SensorManager.SENSOR_DELAY_UI
                                )
                            }
                            resources.getString(R.string.frequency_fast) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                                    SensorManager.SENSOR_DELAY_FASTEST
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val magNa = resources.getString(R.string.magnetometer) + " - NA"
            mag_header.text = magNa
            mag_spinner.visibility = GONE
            mag_x_val.text = resources.getString(R.string.na)
            mag_y_val.text = resources.getString(R.string.na)
            mag_z_val.text = resources.getString(R.string.na)
        }
    }

    private fun registerGyroscope() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            isSensorAvailability = true
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL
            )
            val arrayAdapter =
                ArrayAdapter<String>(
                    this,
                    R.layout.support_simple_spinner_dropdown_item,
                    frequencyArrays
                )
            gyro_spinner.adapter = arrayAdapter
            gyro_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    if (p0 != null) {

                        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                            sensorManager.unregisterListener(
                                this@MainActivity,
                                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                            )
                        }

                        when (p0.getItemAtPosition(p2).toString()) {
                            resources.getString(R.string.frequency_normal) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                                    SensorManager.SENSOR_DELAY_NORMAL
                                )
                            }
                            resources.getString(R.string.frequency_game) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                                    SensorManager.SENSOR_DELAY_GAME
                                )
                            }
                            resources.getString(R.string.frequency_ui) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                                    SensorManager.SENSOR_DELAY_UI
                                )
                            }
                            resources.getString(R.string.frequency_fast) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                                    SensorManager.SENSOR_DELAY_FASTEST
                                )
                            }
                        }
                    }

                }
            }
        } else {
            val gyroNa = resources.getString(R.string.gyroscope) + " - NA"
            gyro_header.text = gyroNa
            gyro_spinner.visibility = GONE
            gyro_x_val.text = resources.getString(R.string.na)
            gyro_y_val.text = resources.getString(R.string.na)
            gyro_z_val.text = resources.getString(R.string.na)
        }
    }

    private fun registerAccelerometer() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            isSensorAvailability = true
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
            )
            val arrayAdapter =
                ArrayAdapter<String>(
                    this,
                    R.layout.support_simple_spinner_dropdown_item,
                    frequencyArrays
                )
            acc_spinner.adapter = arrayAdapter
            acc_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    if (p0 != null) {

                        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                            sensorManager.unregisterListener(
                                this@MainActivity,
                                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                            )
                        }

                        when (p0.getItemAtPosition(p2).toString()) {
                            resources.getString(R.string.frequency_normal) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                    SensorManager.SENSOR_DELAY_NORMAL
                                )
                            }
                            resources.getString(R.string.frequency_game) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                    SensorManager.SENSOR_DELAY_GAME
                                )
                            }
                            resources.getString(R.string.frequency_ui) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                    SensorManager.SENSOR_DELAY_UI
                                )
                            }
                            resources.getString(R.string.frequency_fast) -> {
                                sensorManager.registerListener(
                                    this@MainActivity,
                                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                    SensorManager.SENSOR_DELAY_FASTEST
                                )
                            }
                        }
                    }

                }
            }
        } else {
            val accNa = resources.getString(R.string.acceleration) + " - NA"
            acc_header.text = accNa
            acc_spinner.visibility = GONE
            acc_x_val.text = resources.getString(R.string.na)
            acc_y_val.text = resources.getString(R.string.na)
            acc_z_val.text = resources.getString(R.string.na)
        }
    }

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

    override fun onDestroy() {
        super.onDestroy()
        unRegisterSensors()
        // Stopping the filter
        mainViewModel.geohashRTFilter.stop()
        mainViewModel.removeLocation()
    }

    private fun unRegisterSensors() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensorManager.unregisterListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            )
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorManager.unregisterListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            )
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensorManager.unregisterListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            )
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensorManager.unregisterListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            )
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            sensorManager.unregisterListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            )
        }
    }

}
