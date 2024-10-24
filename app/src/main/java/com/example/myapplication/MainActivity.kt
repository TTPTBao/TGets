package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MainActivity : AppCompatActivity() {

    private lateinit var mqttClient: MqttAndroidClient
    private val mqttServerURI = "tcp://broker.hivemq.com:1883"  // MQTT broker URL
    private val topic = "sensor/data"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val salinityTextView = findViewById<TextView>(R.id.salinityTextView)
        val temperatureTextView = findViewById<TextView>(R.id.temperatureTextView)
        val phTextView = findViewById<TextView>(R.id.phTextView)
        val waterLevelTextView = findViewById<TextView>(R.id.waterLevelTextView)

        mqttClient = MqttAndroidClient(this, mqttServerURI, "AndroidClient")

        // Connect to the MQTT broker
        mqttClient.connect(MqttConnectOptions(), null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                mqttClient.subscribe(topic, 1)
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Toast.makeText(applicationContext, "Failed to connect to MQTT", Toast.LENGTH_SHORT).show()
            }
        })

        // Listen for incoming messages
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {}
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val sensorData = message.toString().split(",")
                val salinity = sensorData[0].toDouble()
                val temperature = sensorData[1].toDouble()
                val ph = sensorData[2].toDouble()
                val waterLevel = sensorData[3].toDouble()

                // Update UI
                salinityTextView.text = "Salinity: $salinity%O"
                temperatureTextView.text = "Temperature: $temperature°C"
                phTextView.text = "pH: $ph"
                waterLevelTextView.text = "Water Level: $waterLevel cm"

                // Check for alerts
                checkAlerts(salinity, temperature, ph)
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
    }

    private fun checkAlerts(salinity: Double, temperature: Double, ph: Double) {
        when {
            salinity > 33 -> showAlert("ĐỘ MẶN QUÁ CAO! (Nguy hiểm)")
            salinity in 26.0..33.0 -> showAlert("ĐỘ MẶN CAO!")
            salinity in 10.0..25.0 -> showAlert("ĐỘ MẶN TỐT!")
            salinity in 5.0..9.0 -> showAlert("ĐỘ MẶN THẤP!")
            salinity < 5 -> showAlert("ĐỘ MẶN QUÁ THẤP! (Nguy hiểm)")
        }

        when {
            temperature > 35 -> showAlert("NHIỆT ĐỘ QUÁ CAO! (Nguy hiểm)")
            temperature in 30.0..35.0 -> showAlert("NHIỆT ĐỘ CAO!")
            temperature in 20.0..30.0 -> showAlert("NHIỆT ĐỘ TỐT!")
            temperature in 15.0..20.0 -> showAlert("NHIỆT ĐỘ THẤP!")
            temperature < 15 -> showAlert("NHIỆT ĐỘ QUÁ THẤP! (Nguy hiểm)")
        }

        when {
            ph > 9 -> showAlert("ĐỘ PH QUÁ CAO! (Nguy hiểm)")
            ph in 8.6..9.0 -> showAlert("ĐỘ PH CAO!")
            ph in 7.5..8.5 -> showAlert("ĐỘ PH TỐT!")
            ph in 7.0..7.4 -> showAlert("ĐỘ PH THẤP!")
            ph < 7 -> showAlert("ĐỘ PH QUÁ THẤP! (Nguy hiểm)")
        }
    }

    private fun showAlert(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
