package com.example.bluetoothapp.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class InternalSensorRepository(
    applicationContext : Context
)
{
    private val sensorManager: SensorManager =
        applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var gyroscopeSensor: Sensor?
    private var linearAccelerationSensor: Sensor?

    private val _gyroscopeData = MutableLiveData<FloatArray>()
    val gyroscopeData: LiveData<FloatArray> get() = _gyroscopeData

    private val _linearAccelerationData = MutableLiveData<FloatArray>()
    val linearAccelerationData: LiveData<FloatArray> get() = _linearAccelerationData

    init {
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }

    fun startListening() : Boolean {
        if (gyroscopeSensor == null || linearAccelerationSensor == null) {
            return false;
        }
        sensorManager.registerListener(sensorEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorEventListener, linearAccelerationSensor, SensorManager.SENSOR_DELAY_UI)
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
                        _gyroscopeData.value = event.values
                    }
                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        _linearAccelerationData.value = event.values
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            TODO("Not yet implemented")
        }
    }

}