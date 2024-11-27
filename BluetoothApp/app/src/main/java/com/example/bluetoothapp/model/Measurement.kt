package com.example.bluetoothapp.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import com.example.bluetoothapp.data.InternalSensorRepository
import com.example.bluetoothapp.data.PolarSensorRepository
import io.reactivex.rxjava3.core.Single
import java.time.LocalDateTime

class Measurement (
    private var _id : Int = 0,
    private var _measured : LocalDateTime = LocalDateTime.now(),
    private var _linearFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _fusionFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _applicationContext : Context,
    private var _finished: Boolean = false
){

    val id: Int
        get() = _id

    val measured: LocalDateTime
        get() = _measured

    val linearFilteredSamples: MutableList<Float>
        get() = _linearFilteredSamples

    val fusionFilteredSamples : MutableList<Float>
        get() = _fusionFilteredSamples

    val finished: Boolean
        get() = _finished

    private val internalSensorRepository : InternalSensorRepository = InternalSensorRepository(_applicationContext)
    private val polarSensorRepository : PolarSensorRepository = PolarSensorRepository(_applicationContext)

    init {
        var currentLinearSample : Float = -1f
        var currentAngularSample : Float = -1f
        val sampleMediator = MediatorLiveData<Pair<Float, Float>>()
        sampleMediator.addSource(internalSensorRepository.linearAccelerationData) {
            val linearSample = calculateElevationLinear(it[1], it[2])
            applyLinearFilter(linearSample)
            if (currentAngularSample >= 0) {
                sampleMediator.value = Pair(linearSample, currentLinearSample)
                currentAngularSample = -1f
            }
            else {
                currentLinearSample = linearSample
            }
        }
       sampleMediator.addSource(internalSensorRepository.gyroscopeData) {
            val angularSample = calculateElevationAngular()
           if (currentLinearSample >= 0) {
               sampleMediator.value = Pair(currentLinearSample, angularSample)
               currentLinearSample = -1f
           }
           else {
               currentAngularSample = angularSample
           }
       }
        sampleMediator.observeForever( {
            applyFusionFilter(it.first, it.second)
        })
    }

    fun startRecording() {
        internalSensorRepository.startListening()
    }

    fun stopRecording() {
        internalSensorRepository.stopListening()
        _finished = true
    }

    private fun calculateElevationLinear(yValue: Float, zValue: Float) : Float {
        TODO()
    }

    private fun calculateElevationAngular() : Float {
        TODO()
    }

    private fun applyLinearFilter(linearSample : Float) {
        TODO()
    }

    private fun applyFusionFilter(linearSample: Float, angularSample: Float) {
        TODO()
    }

    fun hasRequiredPermissions(): Boolean {
        val context = _applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun searchForDevices() : Single<List<Device>> {
        return polarSensorRepository.searchForDevices()
    }

    fun connectToPolarDevice(deviceId: String) {
        polarSensorRepository.connectToDevice(deviceId)
    }

    fun startStreaming(deviceId: String) {
        polarSensorRepository.startAccStreaming(deviceId)
        polarSensorRepository.startGyroStreaming(deviceId)
    }

    fun stopStreaming(deviceId: String) {
        polarSensorRepository.stopStreaming()
        _finished = true
    }
    fun disconnectFromPolarDevice(deviceId: String) {
        polarSensorRepository.disconnectFromDevice(deviceId)
    }
}