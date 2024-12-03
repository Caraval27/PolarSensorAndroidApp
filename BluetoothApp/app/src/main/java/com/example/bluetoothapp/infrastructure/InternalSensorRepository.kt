package com.example.bluetoothapp.infrastructure

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import com.example.bluetoothapp.application.MeasurementService
import com.example.bluetoothapp.domain.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InternalSensorRepository(
    applicationContext : Context
)
{
    private val sensorManager: SensorManager =
        applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gyroscopeSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _gyroscopeData = MutableStateFlow(SensorData())
    val gyroscopeData: StateFlow<SensorData>
        get() = _gyroscopeData

    private val _accelerometerData = MutableStateFlow(SensorData())
    val accelerometerData: StateFlow<SensorData>
        get() = _accelerometerData

    fun startListening() {
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (accelerometerSensor != null) {
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                when (event.sensor.type) {
                    Sensor.TYPE_GYROSCOPE -> {
                        _gyroscopeData.value = SensorData(xValue = event.values[0],
                            yValue = event.values[1],
                            zValue = event.values[2],
                            timeStamp = event.timestamp)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        _accelerometerData.value = SensorData(
                            xValue = event.values[0],
                            yValue = event.values[1],
                            zValue = event.values[2],
                            timeStamp = event.timestamp)
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

}