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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.mutableStateOf
import kotlin.math.PI
import kotlin.math.atan
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
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MeasurementService(
    private var _applicationContext : Context,
) {
    private val _linearFilteredSamples: MutableStateFlow<List<Float>> = MutableStateFlow(emptyList())
    val linearFilteredSamples: StateFlow<List<Float>>
        get() = _linearFilteredSamples.asStateFlow()

    private val _fusionFilteredSamples : MutableStateFlow<List<Float>> = MutableStateFlow(emptyList())
    val fusionFilteredSamples: StateFlow<List<Float>>
        get() = _fusionFilteredSamples.asStateFlow()

    private var _lastAngularSample: Float = -1f

    private val internalSensorRepository : InternalSensorRepository = InternalSensorRepository(_applicationContext)
    private val polarSensorRepository : PolarSensorRepository = PolarSensorRepository(_applicationContext)
    private val measurementDbRepository : MeasurementDbRepository = MeasurementDbRepository(_applicationContext)

    private val measurementJob = Job()

    private val measurementScope = CoroutineScope(Dispatchers.Default + measurementJob)


    companion object {
        const val SENSOR_DELAY = 60000
    }

    init {
        //var currentLinearSample : Float? = null
        //var currentAngularSample : Float? = null

        /*val sampleMediator = MediatorLiveData<Pair<Float, Float>>()

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
        }*/
    }

    private fun calculateElevationLinear(yValue: Float, zValue: Float) : Float {
        val quota = zValue / yValue
        val angleDegrees = atan(quota) / PI * 180 //kanske får ändras sen
        return angleDegrees.toFloat()
    }

    private fun calculateElevationAngular(zValue: Float) : Float {
        val timeDelta = SENSOR_DELAY / 10.0f.pow(6)
        val angle = _lastAngularSample + zValue * timeDelta
        _lastAngularSample = angle
        return angle
    }

    private fun applyLinearFilter(linearSample : Float, filterFactor: Float) {
        val linearFilteredSample = filterFactor * linearSample + (1 - filterFactor) * _linearFilteredSamples.value.last()
        _linearFilteredSamples.value += linearFilteredSample
    }

    private fun applyFusionFilter(linearSample: Float, angularSample: Float) {
        val filterFactor = 0.98f
        val fusionFilteredSample = filterFactor * linearSample + (1 - filterFactor) * angularSample
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
        measurementScope.launch {
            internalSensorRepository.linearAccelerationData.zip(internalSensorRepository.gyroscopeData) { linearAcceleration, angularVelocity ->
                Pair(linearAcceleration, angularVelocity)
            }.collect { sample ->
                val linearSample = calculateElevationLinear(sample.first[1], sample.first[2])
                applyLinearFilter(linearSample, 0.1f)
                val angularSample = calculateElevationAngular(sample.second[2])
                applyFusionFilter(linearSample, angularSample)
            }
        }
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
        measurementScope.launch {
            polarSensorRepository.linearAccelerationData.zip(polarSensorRepository.gyroscopeData) { linearAcceleration, angularVelocity ->
                Pair(linearAcceleration, angularVelocity)
            }.collect { sample ->
                val linearSample = calculateElevationLinear(sample.first[1], sample.first[2])
                applyLinearFilter(linearSample, 0.1f)
                val angularSample = calculateElevationAngular(sample.second[2])
                applyFusionFilter(linearSample, angularSample)
            }
        }
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