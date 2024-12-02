package com.example.bluetoothapp.application

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.bluetoothapp.infrastructure.InternalSensorRepository
import com.example.bluetoothapp.infrastructure.MeasurementDbRepository
import com.example.bluetoothapp.infrastructure.PolarSensorRepository
import com.example.bluetoothapp.domain.Measurement
import android.Manifest
import android.location.LocationManager
import androidx.activity.result.ActivityResultLauncher
import android.os.Environment
import android.util.Log
import kotlin.math.pow
import com.example.bluetoothapp.domain.Device
import com.example.bluetoothapp.infrastructure.MeasurementData
import com.example.bluetoothapp.infrastructure.MeasurementFileRepository
import com.example.bluetoothapp.infrastructure.SampleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.atan2

class MeasurementService(
    private var _applicationContext : Context,
) {
    private val _measurement : MutableStateFlow<Measurement> = MutableStateFlow(Measurement())
    val measurement: StateFlow<Measurement>
        get() = _measurement

    private var _lastAngularSample: Float? = null

    private val _internalSensorRepository : InternalSensorRepository = InternalSensorRepository(_applicationContext)
    private val _polarSensorRepository : PolarSensorRepository = PolarSensorRepository(_applicationContext)
    private val _measurementDbRepository : MeasurementDbRepository = MeasurementDbRepository(_applicationContext)
    private val _measurementFileRepository : MeasurementFileRepository = MeasurementFileRepository(_applicationContext)

    val connectedDevice: StateFlow<String> = _polarSensorRepository.connectedDevice

    companion object {
        const val SENSOR_DELAY = 60000
    }

    init {
        val measurementScope = CoroutineScope(Dispatchers.Default + Job())

        measurementScope.launch {
            _internalSensorRepository.linearAccelerationData
                .filter { it.isNotEmpty() }
                .zip(_internalSensorRepository.gyroscopeData
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
            _polarSensorRepository.linearAccelerationData
                .filter { it.isNotEmpty() }
                .zip(_polarSensorRepository.gyroscopeData
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
        if (_measurement.value.linearFilteredSamples.isNotEmpty()) {
            linearFilteredSample = filterFactor * linearSample + (1 - filterFactor) * _measurement.value.linearFilteredSamples.last()
            //Log.d("MeasurementService", "linear sample: " + linearSample + " last linear sample: " + _linearFilteredSamples.value.last() + " filtered sample: " + linearFilteredSample)
        }

        _measurement.value = _measurement.value.copy(linearFilteredSamples = _measurement.value.linearFilteredSamples + linearFilteredSample)
    }

    private fun applyFusionFilter(linearSample: Float, angularSample: Float) {
        val filterFactor = 0.98f
        val fusionFilteredSample = filterFactor * linearSample + (1 - filterFactor) * angularSample
        //Log.d("MeasurementService", "linear sample: " + linearSample + " angular sample: " + angularSample + " result: " + fusionFilteredSample)
        _measurement.value = _measurement.value.copy(fusionFilteredSamples = _measurement.value.fusionFilteredSamples + fusionFilteredSample)
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
        _measurementDbRepository.insertMeasurement(measurementData)
    }

    suspend fun getMeasurementsHistory() : MutableList<Measurement> {
        val measurementsData = _measurementDbRepository.getMeasurements()

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
        _measurement.value = Measurement()
        _lastAngularSample = null
        _internalSensorRepository.startListening()
    }

    suspend fun stopInternalRecording(measurement: Measurement) {
        _internalSensorRepository.stopListening()
        insertMeasurement(measurement)
    }

    fun hasRequiredBluetoothPermissions(): Boolean {
        /*val context = _applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }*/
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            else -> arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        val context = _applicationContext

        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        /*return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }*/
    }

    fun requestPermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            else -> arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        requestPermissionLauncher.launch(permissions)
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = _applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun searchForDevices() : Flow<Device> {
        return _polarSensorRepository.searchForDevices()
    }

    fun connectToPolarDevice(deviceId: String) {
        _polarSensorRepository.connectToDevice(deviceId)
    }

    fun isDeviceConnected(deviceId: String) : Boolean {
        return _polarSensorRepository.isDeviceConnected(deviceId)
    }

    fun startPolarRecording(deviceId: String) {
        _measurement.value = Measurement()
        _lastAngularSample = null
        _polarSensorRepository.startAccStreaming(deviceId)
        _polarSensorRepository.startGyroStreaming(deviceId)
    }

    suspend fun stopPolarRecording(measurement: Measurement) {
        _polarSensorRepository.stopStreaming()
        insertMeasurement(measurement)
    }

    fun disconnectFromPolarDevice(deviceId: String) {
        _polarSensorRepository.disconnectFromDevice(deviceId)
    }

    fun exportMeasurement(measurement: Measurement) : Boolean {
        Log.d("MeasurementService", "Write permission granted: " + ContextCompat.checkSelfPermission(_applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        Log.d("MeasurementService", "Manage files permission granted: " + Environment.isExternalStorageManager())
        var csvContent = "Linear, Fusion\n"
        for (i in measurement.linearFilteredSamples.indices) {
            csvContent += measurement.linearFilteredSamples[i].toString() + ", " + measurement.fusionFilteredSamples[i].toString() + "\n"
        }
        return _measurementFileRepository.exportCsvToDownloads("ElevationAngle" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")), csvContent)
    }

    fun clearDb() {
        _measurementDbRepository.clearDb()
    }
}