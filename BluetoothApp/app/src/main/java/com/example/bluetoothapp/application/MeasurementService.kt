package com.example.bluetoothapp.application

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import com.example.bluetoothapp.infrastructure.InternalSensorRepository
import com.example.bluetoothapp.infrastructure.MeasurementDbRepository
import com.example.bluetoothapp.infrastructure.PolarSensorRepository
import com.example.bluetoothapp.domain.Measurement
import io.reactivex.rxjava3.core.Single
import android.Manifest
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow
import com.example.bluetoothapp.domain.Device

class MeasurementService(
    private var _applicationContext : Context,
    private var _measurement : Measurement = Measurement()
) {

    private val internalSensorRepository : InternalSensorRepository = InternalSensorRepository(_applicationContext)
    private val polarSensorRepository : PolarSensorRepository = PolarSensorRepository(_applicationContext)
    private val measurementDbRepository : MeasurementDbRepository = MeasurementDbRepository(_applicationContext)

    companion object {
        const val SENSOR_DELAY = 60000
    }

    init {
        var currentLinearSample : Float? = null
        var currentAngularSample : Float? = null

        val sampleMediator = MediatorLiveData<Pair<Float, Float>>()

        //felhantering krävs sen ifall en sensor misslyckades att producera ett värde i följden
        // just nu kommer det värdet bara att hoppas över

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

    private fun calculateElevationLinear(yValue: Float, zValue: Float) : Float {
        val quota = zValue / yValue
        val angleDegrees = atan(quota) / PI * 180 //kanske får ändras sen
        return angleDegrees.toFloat()
    }

    private fun calculateElevationAngular(zValue: Float) : Float {
        val timeDelta = SENSOR_DELAY / 10.0f.pow(6)
        val angle = _measurement.lastAngularSample + zValue * timeDelta
        _measurement.lastAngularSample = angle
        return angle
    }

    private fun applyLinearFilter(linearSample : Float, filterFactor: Float) {
        val linearFilteredSample = filterFactor * linearSample + (1 - filterFactor) * _measurement.linearFilteredSamples.last()
        _measurement.addLinearFilteredSample(linearFilteredSample)
    }

    private fun applyFusionFilter(linearSample: Float, angularSample: Float) {
        val filterFactor = 0.98f
        val fusionFilteredSample = filterFactor * linearSample + (1 - filterFactor) * angularSample
        _measurement.addFusionFilteredSample(fusionFilteredSample)
    }

    suspend fun getMeasurementsHistory() : MutableList<Measurement> {
        val measurementsData = measurementDbRepository.getMeasurements()

        if (measurementsData.isNotEmpty()) {
            return measurementsData.map { measurementData ->
                measurementData.let { data ->
                    Measurement(
                        _id = data.id,
                        _measured = data.timeMeasured,
                        _linearFilteredSamples = data.sampleData.map { it.singleFilterValue }
                            .toMutableList(),
                        _fusionFilteredSamples = data.sampleData.map { it.fusionFilterValue }
                            .toMutableList(),
                        _finished = true
                    )
                }
            }.toMutableList()
        } else {
            return mutableListOf()
        }
    }

    fun startInternalRecording() {
        internalSensorRepository.startListening()
    }

    fun stopInternalRecording() {
        internalSensorRepository.stopListening()
        _measurement.finished = true
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

    fun startPolarRecording(deviceId: String) {
        polarSensorRepository.startAccStreaming(deviceId)
        polarSensorRepository.startGyroStreaming(deviceId)
    }

    fun stopPolarRecording(deviceId: String) {
        polarSensorRepository.stopStreaming()
        _measurement.finished = true
    }

    fun disconnectFromPolarDevice(deviceId: String) {
        polarSensorRepository.disconnectFromDevice(deviceId)
    }
}