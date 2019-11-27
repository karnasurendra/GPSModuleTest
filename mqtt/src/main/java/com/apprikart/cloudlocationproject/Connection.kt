package com.apprikart.cloudlocationproject

import android.content.*
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject

open class Connection {

    private lateinit var connectionCallbacks: ConnectionCallbacks
    private val connectionError: String = "No Internet Connection"
    private val permissionError: String = "Permissions Required"
    private lateinit var mqttHelper: MqttHelper
    private lateinit var mContext: Context
    private lateinit var uniqueId: String
    private lateinit var serviceBroadcastReceiver: BroadcastReceiver
    private lateinit var sensorDataCollector: SensorDataCollector

    fun connect(
        context: Context,
        connectionType: String,
        server: String,
        user: String,
        password: String,
        port: String,
        uniqueId: String,
        timeInterval: String
    ) {
        mContext = context
        this.uniqueId = uniqueId
        /*This is for Checking internet Connection*/
        val connectionVal = Utils.getConnectionType(mContext)
        if (connectionVal == 1 || connectionVal == 2) {
            // This is for Checking Location permission is available for the app or Not
            if (Utils.hasPermissions(mContext)) {
                constructParamsAndConnectServer(
                    connectionType,
                    server,
                    user,
                    password,
                    port,
                    timeInterval
                )
            } else {
                connectionCallbacks.permissionFailure(permissionError)
            }
        } else {
            connectionCallbacks.internetFailure(connectionError)
        }
    }

    private fun constructParamsAndConnectServer(
        connectionType: String,
        server: String,
        user: String,
        password: String,
        port: String,
        timeInterval: String
    ) {
        val defaultServer = when (connectionType) {
            ServerConstants.MQTT_OVER_SSL -> "ssl"
            ServerConstants.WEBSOCKETS -> "wss"
            else -> "tcp"
        }
        val serverUrl = "$defaultServer://$server:$port"
        startMqtt(serverUrl, user, password, timeInterval)
    }

    fun startMqtt(
        serverUrl: String,
        user: String,
        password: String,
        timeInterval: String
    ) {
        Log.d("MqttHelper::", "Checking Details :: $serverUrl $user $password")
        mqttHelper = MqttHelper(mContext, serverUrl, user, password)
        mqttHelper.setCallBacks(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                connectionCallbacks.connectionStatus(true)
                registerBroadCastForLocationUpdates()
                // Connecting to LocationService as it is Connected to MQTT
                val locationIntent = Intent(mContext, LocationService::class.java)
                locationIntent.putExtra("TimeInterval", timeInterval.toInt())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(locationIntent)
                } else {
                    mContext.startService(locationIntent)
                }

                // Sensors
                initializeAndStartSensor()

            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
            }

            override fun connectionLost(cause: Throwable?) {
                connectionCallbacks.connectionStatus(false)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }

        })
    }

    private fun initializeAndStartSensor() {
        sensorDataCollector = MadgwikProvider(
            mContext
                .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        )
        sensorDataCollector.start()

    }

    private fun registerBroadCastForLocationUpdates() {
        serviceBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action == "Location Updates") {
                    val jsonObject = JSONObject()
                    jsonObject.put("ID", uniqueId)
                    jsonObject.put("Latitude", p1.getDoubleExtra("Latitude", 0.0))
                    jsonObject.put("Longitude", p1.getDoubleExtra("Longitude", 0.0))
                    Log.d(
                        "LocationService::--",
                        "Location issue registerBroadCastForLocationUpdates $jsonObject"
                    )
                    sendDataThroughMqtt(jsonObject)
                }
            }
        }
        val intentFilter = IntentFilter("Location Updates")
        mContext.registerReceiver(serviceBroadcastReceiver, intentFilter)
    }


    fun setConnectionCallbacks(connectionCallbacks: ConnectionCallbacks) {
        this.connectionCallbacks = connectionCallbacks
    }

    fun disConnectFromServer() {
        if (mqttHelper.disConnect()) {
            removeServiceAndBroadCast()
            sensorDataCollector.stop() // This is to Unregister the Sensor
        }
    }

    private fun removeServiceAndBroadCast() {
        mContext.stopService(Intent(mContext, LocationService::class.java))
        mContext.unregisterReceiver(serviceBroadcastReceiver)
    }

    private fun sendDataThroughMqtt(jsonObject: JSONObject) {
        mqttHelper.publish(jsonObject.toString())
    }

    interface ConnectionCallbacks {
        fun internetFailure(error: String)
        fun permissionFailure(error: String)
        fun connectionStatus(isConnected: Boolean)
    }

}