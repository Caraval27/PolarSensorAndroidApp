package com.example.bluetoothapp.infrastructure

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.example.bluetoothapp.application.MeasurementService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InternalSensorRepository(
    applicationContext : Context
)
{
    private val sensorManager: SensorManager =
        applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var gyroscopeSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var linearAccelerationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val _gyroscopeData = MutableStateFlow(floatArrayOf())
    val gyroscopeData: StateFlow<FloatArray>
        get() = _gyroscopeData.asStateFlow()

    private val _linearAccelerationData = MutableStateFlow(floatArrayOf())
    val linearAccelerationData: StateFlow<FloatArray>
        get() = _linearAccelerationData.asStateFlow()

    fun startListening() : Boolean {
        if (gyroscopeSensor == null || linearAccelerationSensor == null) {
            return false;
        }
        sensorManager.registerListener(sensorEventListener, gyroscopeSensor, MeasurementService.SENSOR_DELAY)
        sensorManager.registerListener(sensorEventListener, linearAccelerationSensor, MeasurementService.SENSOR_DELAY)
        return true;
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                when (event.sensor.type) {
                    Sensor.TYPE_GYROSCOPE -> {
                        _gyroscopeData.value = event.values.clone()
                    }
                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        _linearAccelerationData.value = event.values.clone()
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            //kanske fixa sen
        }
    }

}