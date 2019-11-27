package com.apprikart.rotationmatrixdemo.views

import android.Manifest
import android.annotation.SuppressLint
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
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.apprikart.rotationmatrixdemo.R
import com.apprikart.rotationmatrixdemo.Utils
import com.apprikart.rotationmatrixdemo.filters.Coordinates
import com.apprikart.rotationmatrixdemo.filters.GPSAccKalmanFilter
import com.apprikart.rotationmatrixdemo.models.SensorGpsDataItem
import com.apprikart.rotationmatrixdemo.models.sensorvaluemodels.*
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.TimeUnit
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
                        0 -> acc_x_val.text = event.values[i].toString()
                        1 -> acc_y_val.text = event.values[i].toString()
                        2 -> acc_z_val.text = event.values[i].toString()
                    }
                }
                val accNow = android.os.SystemClock.elapsedRealtimeNanos()
                val accNowMs =
                    Utils.nano2milli(
                        accNow
                    )

                // Here Creating an Model of Acceleration and adding those values to Queue
                val accModel =
                    AccModel(
                        Utils.ACCELERATION,
                        acc_spinner.selectedItem.toString(),
                        accNowMs.toDouble(),
                        event.values[0].toString(),
                        event.values[1].toString(),
                        event.values[2].toString()
                    )

                accQueue.add(accModel)


            }
            Sensor.TYPE_GYROSCOPE -> {
                for (i in event.values.indices) {
                    when (i) {
                        0 -> gyro_x_val.text = event.values[i].toString()
                        1 -> gyro_y_val.text = event.values[i].toString()
                        2 -> gyro_z_val.text = event.values[i].toString()
                    }
                }

                val gyroNow = android.os.SystemClock.elapsedRealtimeNanos()
                val gyroNowMs =
                    Utils.nano2milli(
                        gyroNow
                    )

                // Here Creating an Model of Gyroscope and adding those values to Queue
                val gyroModel =
                    GyroModel(
                        Utils.GYROSCOPE,
                        gyro_spinner.selectedItem.toString(),
                        gyroNowMs.toDouble(),
                        event.values[0].toString(),
                        event.values[1].toString(),
                        event.values[2].toString()
                    )

                gyroQueue.add(gyroModel)

            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                for (i in event.values.indices) {
                    when (i) {
                        0 -> mag_x_val.text = event.values[i].toString()
                        1 -> mag_y_val.text = event.values[i].toString()
                        2 -> mag_z_val.text = event.values[i].toString()
                    }
                }

                val magNow = android.os.SystemClock.elapsedRealtimeNanos()
                val magNowMs =
                    Utils.nano2milli(
                        magNow
                    )

                // Here Creating an Model of Magnetometer and adding those values to Queue
                val magModel =
                    MagModel(
                        Utils.MAGNETOMETER,
                        mag_spinner.selectedItem.toString(),
                        magNowMs.toDouble(),
                        event.values[0].toString(),
                        event.values[1].toString(),
                        event.values[2].toString()
                    )

                magQueue.add(magModel)
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                for (i in event.values.indices) {
                    when (i) {
                        0 -> linear_x_val.text = event.values[i].toString()
                        1 -> linear_y_val.text = event.values[i].toString()
                        2 -> linear_z_val.text = event.values[i].toString()
                    }
                }

                val lANow = android.os.SystemClock.elapsedRealtimeNanos()
                val lANowMs =
                    Utils.nano2milli(
                        lANow
                    )

                // Here Creating an Model of Linear Acceleration and adding those values to Queue
                val linearAccModel =
                    LinearAccModel(
                        Utils.LINEAR_ACCELERATION,
                        linear_acc_spinner.selectedItem.toString(),
                        lANowMs.toDouble(),
                        event.values[0].toString(),
                        event.values[1].toString(),
                        event.values[2].toString()
                    )

                linearAccQueue.add(linearAccModel)

                // Converting the event values to an Array
                System.arraycopy(event.values, 0, linAcceleration, 0, event.values.size)

                // Multiplying the inverted Rotation Matrix values with the linear acceleration sensor values
                android.opengl.Matrix.multiplyMV(
                    axesAcceleration,
                    0,
                    invertedRotationMatrix,
                    0,
                    linAcceleration,
                    0
                )

                // Displaying the data in Text views
                rotation_matrix_x_val.text = axesAcceleration[0].toString()
                rotation_matrix_y_val.text = axesAcceleration[1].toString()
                rotation_matrix_z_val.text = axesAcceleration[2].toString()

                val laAfterRotNow = android.os.SystemClock.elapsedRealtimeNanos()
                val laAfterRotNowMs =
                    Utils.nano2milli(
                        laAfterRotNow
                    )

                // Here Creating an Model of Linear Acceleration after rotation and adding those values to Queue
                val laAfterRotation =
                    LAAfterRotation(
                        Utils.LINEAR_ACCELERATION_AFTER_ROTATION,
                        laAfterRotNowMs.toDouble(),
                        axesAcceleration[0].toString(),
                        axesAcceleration[1].toString(),
                        axesAcceleration[2].toString()
                    )

                linearAccAfterRotationQueue.add(laAfterRotation)

                // It will initialize once the Location details get triggered
                if (gpsAccKalmanFilter == null) return

                // Creating the SensorDataItem object and Location values not available here so which are the fields are not available made them as Not Initialized
                val sensorGpsDataItem =
                    SensorGpsDataItem(
                        nowMs.toDouble(),
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        axesAcceleration[north].toDouble(),
                        axesAcceleration[east].toDouble(),
                        axesAcceleration[up].toDouble(),
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
                Log.d("MainActivity::", "Sensors Values Rotation Values ${event.values.size}")
                for (i in event.values.indices) {
                    when (i) {
                        0 -> rotation_vector_x_val.text = event.values[i].toString()
                        1 -> rotation_vector_y_val.text = event.values[i].toString()
                        2 -> rotation_vector_z_val.text = event.values[i].toString()
                    }
                }

                val rVNow = android.os.SystemClock.elapsedRealtimeNanos()
                val rVNowMs =
                    Utils.nano2milli(
                        rVNow
                    )

                val rotationVectorModel =
                    RotationVectorModel(
                        Utils.ROTATION_VECTOR,
                        rotation_vec_spinner.selectedItem.toString(),
                        rVNowMs.toDouble(),
                        event.values[0].toString(),
                        event.values[1].toString(),
                        event.values[3].toString()
                    )

                rotationVectorQueue.add(rotationVectorModel)

                // Getting Rotation Matrix values from Rotation Vector Component, which is 16 size array in Matrix form 4 x 4 matrix
                // one dimensions for each axis x, y, and z, plus one dimension to represent the “origin” in the coordinate system. These are known as homogenous coordinates
                SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values)

                // Inverting the 4 x 4 Rotation Matrix Values and saving to invertedRotationMatrix
                android.opengl.Matrix.invertM(invertedRotationMatrix, 0, mRotationMatrix, 0)

            }
        }
    }

    private lateinit var job: Job
    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WAKE_LOCK
    )
    private lateinit var sensorManager: SensorManager
    private val mRotationMatrix = FloatArray(16)
    private val invertedRotationMatrix = FloatArray(16)
    private val linAcceleration = FloatArray(4)
    private val axesAcceleration = FloatArray(4)
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    //This is the declination of the horizontal component of the magnetic field from true north, in degrees
    // Ref link : https://www.youtube.com/watch?v=uN5w24F4hGk
    // Magnetic declination can be calculated using Location latitude, longitude and Altitude
    private var mMagneticDeclination: Double = 0.0
    private var gpsAccKalmanFilter: GPSAccKalmanFilter? = null
    // SensorDataItem will be added to this Queue
    private val mSensorDataQueue: Queue<SensorGpsDataItem> =
        PriorityBlockingQueue()
    private val permissionReqCode = 100
    private val frequencyArrays = arrayOf("Normal", "Game", "UI", "Fast")
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var isSensorAvailability = false
    // Queues to Store sensor values before writing to text values
    private var accQueue: Queue<AccModel> = PriorityBlockingQueue()
    private var gyroQueue: Queue<GyroModel> = PriorityBlockingQueue()
    private var magQueue: Queue<MagModel> = PriorityBlockingQueue()
    private var linearAccQueue: Queue<LinearAccModel> = PriorityBlockingQueue()
    private var rotationVectorQueue: Queue<RotationVectorModel> = PriorityBlockingQueue()
    private var linearAccAfterRotationQueue: Queue<LAAfterRotation> = PriorityBlockingQueue()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPowerLock()
        checkPermissions()

        Log.d("Main::", "Coroutine issue in Before launch Coroutine ${Thread.currentThread()}")
        checkingCouroutine()
        Log.d("Main::", "Coroutine issue in After launch Coroutine ${Thread.currentThread()}")


    }

    private fun checkingCouroutine() = runBlocking {
        // If we do Global scope launch it will do synchronous flow(It means executing the next line even though there is a delay)
        job = GlobalScope.launch {
            postDelayed()
        }

        // If we directly call this method it will suspend the thread itself
//        postDelayed()
    }

    suspend fun postDelayed() {
        delay(2000)
        Log.d("Main::", "Coroutine issue in post Delayed ${Thread.currentThread()}")
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
//                                makeApiCall()
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
                        //allowed
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
        // Initialize location value
        initiateLocation()
        // Initializing the Sensors
        initializeSensors()
    }

    @SuppressLint("WakelockTimeout")
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

        if (isSensorAvailability)
            wakeLock.acquire()   // This one requires specific timeout once the wakelock started, but we need wakelock while running sensors

    }

    private fun initPowerLock() {
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            resources.getString(R.string.wake_lock_tag)
        )
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

    // Location is initializing here
    private fun initiateLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.interval = TimeUnit.SECONDS.toMillis(30)
        locationRequest.fastestInterval = TimeUnit.SECONDS.toMillis(10)
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult == null) return
                for (location in locationResult.locations) {
                    if (location == null) return

                    // This is will tell whether the location is Fake location or original location, so if it is Fake location we need to return
                    if (location.isFromMockProvider) return

                    val xLong: Double = location.longitude
                    val yLat: Double = location.latitude
                    val speed: Double = location.speed.toDouble()
                    //Bearing is the horizontal direction of travel of this device, and is not related to the device orientation.
                    //It is guaranteed to be in the range (0.0, 360.0] if the device has a bearing.
                    //If this location does not have a bearing then 0.0 is returned.
                    val course: Double = location.bearing.toDouble()
                    val xVel: Double = speed * cos(course)
                    val yVel: Double = speed * sin(course)
                    val accuracy: Double = location.accuracy.toDouble()

                    val timeStamp: Long =
                        Utils.nano2milli(
                            location.elapsedRealtimeNanos
                        )
                    //WARNING!!! here should be speed accuracy, but loc.hasSpeedAccuracy()
                    // and loc.getSpeedAccuracyMetersPerSecond() requares API 26
                    val velError = location.accuracy * 0.1

                    updateMagneticDeclination(location, timeStamp)

                    if (gpsAccKalmanFilter == null) {
                        gpsAccKalmanFilter = GPSAccKalmanFilter(
                            false,
                            Coordinates.longitudeToMeters(xLong),
                            Coordinates.latitudeToMeters(yLat),
                            xVel,
                            yVel,
                            Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                            accuracy,
                            timeStamp.toDouble(),
                            Utils.DEFAULT_VEL_FACTOR,
                            Utils.DEFAULT_POS_FACTOR
                        )
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
            }
        }

        // Requesting the Location updates
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)

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
        // Removing the Location updates once the activity is destroyed
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
        unRegisterSensors()
        if (wakeLock.isHeld)
            wakeLock.release()
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
