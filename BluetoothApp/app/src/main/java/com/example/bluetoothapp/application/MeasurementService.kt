package com.example.bluetoothapp.application

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.bluetoothapp.infrastructure.InternalSensorRepository
import com.example.bluetoothapp.infrastructure.MeasurementDbRepository
import com.example.bluetoothapp.infrastructure.PolarSensorRepository
import com.example.bluetoothapp.domain.Measurement
import io.reactivex.rxjava3.core.Single
import android.Manifest
import android.util.Log
import kotlin.math.pow
import com.example.bluetoothapp.domain.Device
import com.example.bluetoothapp.infrastructure.MeasurementData
import com.example.bluetoothapp.infrastructure.SampleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.atan2

class MeasurementService(
    private var _applicationContext : Context,
) {
    private val _linearFilteredSamples: MutableStateFlow<List<Float>> = MutableStateFlow(emptyList())
    val linearFilteredSamples: StateFlow<List<Float>>
        get() = _linearFilteredSamples.asStateFlow()

    private val _fusionFilteredSamples : MutableStateFlow<List<Float>> = MutableStateFlow(emptyList())
    val fusionFilteredSamples: StateFlow<List<Float>>
        get() = _fusionFilteredSamples.asStateFlow()

    private var _lastAngularSample: Float? = null

    private val internalSensorRepository : InternalSensorRepository = InternalSensorRepository(_applicationContext)
    private val polarSensorRepository : PolarSensorRepository = PolarSensorRepository(_applicationContext)
    private val measurementDbRepository : MeasurementDbRepository = MeasurementDbRepository(_applicationContext)

    private val measurementJob = Job()

    private val measurementScope = CoroutineScope(Dispatchers.Default + measurementJob)

    companion object {
        const val SENSOR_DELAY = 60000
    }

    init {
        measurementScope.launch {
            internalSensorRepository.linearAccelerationData
                .filter { it.isNotEmpty() }
                .zip(internalSensorRepository.gyroscopeData
                    .filter { it.isNotEmpty() }) { linearAcceleration, angularVelocity ->
                    Pair(linearAcceleration, angularVelocity)
                }.collect { sample ->
                    //Log.d("MeasurementService","Linear values: " + (sample.first[0]) + " " + sample.first[1] + " " + sample.first[2])
                    //Log.d("MeasurementService","Angular values: " + (sample.second[0]) + " " + sample.second[1] + " " + sample.second[2])
                    val linearSample = calculateElevationLinear(sample.first[1], sample.first[2])
                    applyLinearFilter(linearSample, 0.1f)
                    val angularSample = calculateElevationAngular(sample.second[0])
                    applyFusionFilter(linearSample, angularSample)
                }
        }

        measurementScope.launch {
            polarSensorRepository.linearAccelerationData
                .filter { it.isNotEmpty() }
                .zip(polarSensorRepository.gyroscopeData
                    .filter { it.isNotEmpty() }) { linearAcceleration, angularVelocity ->
                Pair(linearAcceleration, angularVelocity)
            }.collect { sample ->
                val linearSample = calculateElevationLinear(sample.first[1], sample.first[2])
                applyLinearFilter(linearSample, 0.2f)
                val angularSample = calculateElevationAngular(sample.second[0])
                applyFusionFilter(linearSample, angularSample)
            }
        }
    }

    private fun calculateElevationLinear(yValue: Float, zValue: Float) : Float {
        //Log.d("MeasurementService", "Y value: " + yValue + " Z value: " + zValue)
        val angleDegrees = Math.toDegrees(atan2(zValue, yValue).toDouble()) //osäker på om x-värdet måste tas hänsyn till också
        //Log.d("MeasurementService", "angle: " + angleDegrees)
        return angleDegrees.toFloat()
    }

    private fun calculateElevationAngular(xValue: Float) : Float {
        val timeDelta = SENSOR_DELAY / 10.0f.pow(6)
        //Log.d("MeasurementService", "X value: " + xValue)
        var angle = xValue * timeDelta
        _lastAngularSample?.let {
            angle += it
        }
        _lastAngularSample = angle
        //Log.d("MeasurementService", "Angle: " + angle)
        return angle
    }

    private fun applyLinearFilter(linearSample : Float, filterFactor: Float) {
        var linearFilteredSample = linearSample
        if (_linearFilteredSamples.value.isNotEmpty()) {
            linearFilteredSample = filterFactor * linearSample + (1 - filterFactor) * _linearFilteredSamples.value.last()
            //Log.d("MeasurementService", "linear sample: " + linearSample + " last linear sample: " + _linearFilteredSamples.value.last() + " filtered sample: " + linearFilteredSample)
        }
        _linearFilteredSamples.value += linearFilteredSample
    }

    private fun applyFusionFilter(linearSample: Float, angularSample: Float) {
        val filterFactor = 0.98f
        val fusionFilteredSample = filterFactor * linearSample + (1 - filterFactor) * angularSample
        //Log.d("MeasurementService", "linear sample: " + linearSample + " angular sample: " + angularSample + " result: " + fusionFilteredSample)
        _fusionFilteredSamples.value += fusionFilteredSample
    }

    suspend fun insertMeasurement(measurement: Measurement) {
        require(measurement.linearFilteredSamples.size == measurement.fusionFilteredSamples.size) {
            "The two lists must have the same size."
        }

        val measurementData = MeasurementData(
            measurement.id,
            timeMeasured = measurement.timeMeasured,
            sampleData = measurement.linearFilteredSamples.zip(measurement.fusionFilteredSamples) { linear, fusion ->
                SampleData(
                    singleFilterValue = linear,
                    fusionFilterValue = fusion
                )
            }
        )
        measurementDbRepository.insertMeasurement(measurementData)
    }

    suspend fun getMeasurementsHistory() : MutableList<Measurement> {
        val measurementsData = measurementDbRepository.getMeasurements()

        return if (measurementsData.isNotEmpty()) {
             measurementsData.map { measurementData ->
                measurementData.let { data ->
                    Measurement(
                        _id = data.id,
                        _timeMeasured = data.timeMeasured,
                        linearFilteredSamples = data.sampleData.map { it.singleFilterValue }
                            .toMutableList(),
                        fusionFilteredSamples = data.sampleData.map { it.fusionFilterValue }
                            .toMutableList(),
                    )
                }
            }.toMutableList()
        } else {
            mutableListOf()
        }
    }

    fun startInternalRecording() {
        internalSensorRepository.startListening()
    }

    fun stopInternalRecording() {
        internalSensorRepository.stopListening()
        measurementJob.cancel()
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

    fun stopPolarRecording() {
        polarSensorRepository.stopStreaming()
        measurementJob.cancel()
    }

    fun disconnectFromPolarDevice(deviceId: String) {
        polarSensorRepository.disconnectFromDevice(deviceId)
    }

    fun testInsert() : Measurement {
        val linearSamples = mutableListOf(1.0f, 2.0f, 3.0f)
        val fusionSamples = mutableListOf(4.0f, 5.0f, 6.0f)

        return Measurement(
            _timeMeasured = LocalDateTime.now(),
            linearFilteredSamples = linearSamples,
            fusionFilteredSamples = fusionSamples,
        )
    }

    fun clearDb() {
        measurementDbRepository.clearDb()
    }
}