package com.example.bluetoothapp.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import com.example.bluetoothapp.data.InternalSensorRepository
import com.example.bluetoothapp.data.MeasurementData
import com.example.bluetoothapp.data.MeasurementDbRepository
import com.example.bluetoothapp.data.PolarSensorRepository
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Measurement (
    private var id : Int = 0,
    private var _measured : LocalDateTime = LocalDateTime.now(),
    private var _linearFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _fusionFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _applicationContext : Context,
    private var _finished: Boolean = false
){
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
    private val measurementDbRepository : MeasurementDbRepository = MeasurementDbRepository(_applicationContext)

    init {
        var currentLinearSample : Float? = null
        var currentAngularSample : Float? = null

        val sampleMediator = MediatorLiveData<Pair<Float, Float>>()

        //felhantering krävs sen ifall en sensor misslyckades att producera ett värde i följden

        sampleMediator.addSource(internalSensorRepository.linearAccelerationData) {
            val linearSample = calculateElevationLinear(it[1], it[2])
            applyLinearFilter(linearSample)
            currentAngularSample?.let { angularSample ->
                sampleMediator.value = Pair(linearSample, angularSample)
            } ?: run {
                currentLinearSample = linearSample
            }
        }
       sampleMediator.addSource(internalSensorRepository.gyroscopeData) {
           val angularSample = calculateElevationAngular()
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

    suspend fun getMeasurementsHistory() : List<Measurement> {
        val measurementsData = measurementDbRepository.getMeasurements()

        if (measurementsData.isEmpty()) return emptyList()

        return measurementsData.map { measurementData ->
            measurementData.let { data ->
                Measurement(
                    id = data.id,
                    _measured = data.timeMeasured,
                    _linearFilteredSamples = data.sampleData.map { it.singleFilterValue }
                        .toMutableList(),
                    _fusionFilteredSamples = data.sampleData.map { it.fusionFilterValue }
                        .toMutableList(),
                    _applicationContext = _applicationContext,
                    _finished = _finished
                )
            }
        }
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