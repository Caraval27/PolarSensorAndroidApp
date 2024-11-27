package com.example.bluetoothapp.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import com.example.bluetoothapp.data.InternalSensorRepository
import com.example.bluetoothapp.data.MeasurementDbRepository
import com.example.bluetoothapp.data.PolarSensorRepository
import io.reactivex.rxjava3.core.Single
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow

class Measurement (
    private var _id : Int = 0,
    private var _measured : LocalDateTime = LocalDateTime.now(),
    private var _linearFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _fusionFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _lastAngularSample: Float = -1f,
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

    companion object {
        const val SENSOR_DELAY = 60000
    }

    init {
        var currentLinearSample : Float? = null
        var currentAngularSample : Float? = null

        val sampleMediator = MediatorLiveData<Pair<Float, Float>>()

        //felhantering krävs sen ifall en sensor misslyckades att producera ett värde i följden, just nu kommer det värdet bara att hoppas över

        sampleMediator.addSource(internalSensorRepository.linearAccelerationData) {
            val linearSample = calculateElevationLinear(it[1], it[2])
            applyLinearFilter(linearSample, 0.1f)
            currentAngularSample?.let { angularSample ->
                sampleMediator.value = Pair(linearSample, angularSample)
            } ?: run {
                currentLinearSample = linearSample
            }
        }
       sampleMediator.addSource(internalSensorRepository.gyroscopeData) {
           val angularSample = calculateElevationAngular(it[2])
           currentLinearSample?.let { linearSample ->
               sampleMediator.value = Pair(linearSample, angularSample)
           } ?: run {
               currentAngularSample = angularSample
           }
       }
        sampleMediator.observeForever {
            applyFusionFilter(it.first, it.second)
            currentLinearSample = null
            currentAngularSample = null
        }
    }

    fun startRecording() {
        internalSensorRepository.startListening()
    }

    fun stopRecording() {
        internalSensorRepository.stopListening()
        _finished = true
    }

    private fun calculateElevationLinear(yValue: Float, zValue: Float) : Float {
        val quota = zValue / yValue
        val angleDegrees = atan(quota) / PI * 180
        return angleDegrees.toFloat()
    }

    private fun calculateElevationAngular(zValue: Float) : Float {
        val timeDelta = SENSOR_DELAY / 10.0f.pow(6)
        val angle = _lastAngularSample + zValue * timeDelta
        return angle
    }

    private fun applyLinearFilter(linearSample : Float, filterFactor: Float) {
        val linearFilteredSample = filterFactor * linearSample + (1 - filterFactor) * _linearFilteredSamples.last()
        _linearFilteredSamples.add(linearFilteredSample)
    }

    private fun applyFusionFilter(linearSample: Float, angularSample: Float) {
        val filterFactor = 0.98f
        val fusionFilteredSample = filterFactor * linearSample + (1 - filterFactor) * angularSample
        _fusionFilteredSamples.add(fusionFilteredSample)
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