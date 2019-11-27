package com.apprikart.cloudlocationproject

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException

internal class MqttHelper(
    context: Context,
    serverUrl: String,
    private var user: String,
    private var password: String
) {

    private val mContext = context
    private val mqttAndroidClient: MqttAndroidClient
    //    private val serverUri: String = "tcp://soldier.cloudmqtt.com:11967"/*MQTT*/
//    private val serverUri: String = "ssl://soldier.cloudmqtt.com:21967"/*MQTT Over SSL*/
//    private val serverUri: String = "wss://soldier.cloudmqtt.com:31967"/*WebSockets*/
    private val clientId: String = "1234" // This is the Unique id
    private val subscriptionTopic: String = "Location"  // Topic will change based

    init {

        mqttAndroidClient = MqttAndroidClient(mContext, serverUrl, clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
            }

            override fun connectionLost(cause: Throwable?) {
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }

        })

        connect()
    }

    internal fun setCallBacks(callback: MqttCallbackExtended) {
        mqttAndroidClient.setCallback(callback)
    }


    private fun connect() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.userName = user
        mqttConnectOptions.password = password.toCharArray()

        mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MqttHelper::", "Connect Checking in onSuccess")
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = true
                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                subscribeToTopic()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d("MqttHelper::", "Connect Checking in OnFailure ${exception.toString()}")
                mqttAndroidClient.unregisterResources()
            }
        })
    }

    private fun subscribeToTopic() {
        mqttAndroidClient.subscribe(subscriptionTopic, 0, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MqttHelper::", "subscribeToTopic onSuccess")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.d("MqttHelper::", "subscribeToTopic onFailure")
            }
        })
    }

    internal fun publish(message: String) {
        val topic = "foo/bar"
        val encodedMessage: ByteArray
        try {
            encodedMessage = message.toByteArray()
            val mqttMessage = MqttMessage(encodedMessage)
            mqttAndroidClient.publish(topic, mqttMessage)
        } catch (e: UnsupportedEncodingException) {
            Log.d("MqttHelper::", "Exception ${e.message}")
        }

    }

    internal fun disConnect(): Boolean {
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.disconnect()
            return true
        }
        return false
    }

}