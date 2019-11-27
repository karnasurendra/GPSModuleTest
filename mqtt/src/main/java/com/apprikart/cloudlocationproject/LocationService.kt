package com.apprikart.cloudlocationproject

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit
import android.app.Notification
import android.os.Build
import android.app.NotificationManager
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.content.Context
import android.graphics.Color

class LocationService : Service() {

    private val mBinder = MyBinder()
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var timeInterval: Int = 30

    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        initiateLocation()
        Toast.makeText(this@LocationService, "onStartCommand", Toast.LENGTH_SHORT).show()
        if (intent != null) {
            if (intent.hasExtra("TimeInterval")){
                timeInterval = intent.getIntExtra("TimeInterval",30)
            }
        }
        initiateLocation()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground()
        else
            startForeground(1, Notification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("NewApi")
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "com.apprikart.cloudlocationproject"
        val channelName = "My Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)

    }

    private fun initiateLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.interval = TimeUnit.SECONDS.toMillis(timeInterval.toLong())
        locationRequest.fastestInterval = TimeUnit.SECONDS.toMillis(10)
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult == null)
                    return
                for (location in locationResult.locations) {
                    if (location == null)
                        return
                    val intent = Intent()
                    intent.action = "Location Updates"
                    intent.putExtra("Latitude",location.latitude)
                    intent.putExtra("Longitude",location.longitude)
                    Log.d("LocationService::--", "Location issue onLocationResult lat ${location.latitude} long ${location.longitude}")
                    sendBroadcast(intent)
                }

            }
        }
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    inner class MyBinder : Binder() {
        fun getService(): LocationService {
            return this@LocationService
        }
    }


}