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
import kotlin.math.pow
import com.example.bluetoothapp.domain.Device
import com.example.bluetoothapp.domain.Sample
import com.example.bluetoothapp.infrastructure.MeasurementFileRepository
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

    private var _lastGyroscopeTimeStamp: Long = -1

    private val _internalSensorRepository : InternalSensorRepository = InternalSensorRepository(_applicationContext)
    private val _polarSensorRepository : PolarSensorRepository = PolarSensorRepository(_applicationContext)
    private val _measurementDbRepository : MeasurementDbRepository = MeasurementDbRepository(_applicationContext)
    private val _measurementFileRepository : MeasurementFileRepository = MeasurementFileRepository(_applicationContext)

    val connectedDevice: StateFlow<String> = _polarSensorRepository.connectedDevice

    companion object {
        const val SENSOR_DELAY = 20000
    }

    init {
        val measurementScope = CoroutineScope(Dispatchers.Default + Job())

        measurementScope.launch {
            _internalSensorRepository.accelerometerData
                .filter { it.timeStamp >= 0 }
                .zip(_internalSensorRepository.gyroscopeData
                    .filter { it.timeStamp >= 0 }) { accelerometerData, gyroscopeData ->
                    Pair(accelerometerData, gyroscopeData)
                }.collect { sensorData ->
                    //Log.d("MeasurementService","Linear values: " + sensorData.first.xValue + " " + sensorData.first.yValue + " " + sensorData.first.zValue)
                    //Log.d("MeasurementService","Angular values: " + sensorData.second.xValue + " " + sensorData.second.yValue + " " + sensorData.second.zValue)

                    val linearValue = calculateElevationAccelerometer(
                        sensorData.first.yValue,
                        sensorData.first.zValue
                    )
                    val singleFilteredValue = applySingleFilter(linearValue)

                    _measurement.value = _measurement.value.copy(
                        singleFilteredSamples = _measurement.value.singleFilteredSamples + Sample(
                            value = singleFilteredValue,
                            sequenceNumber = (_measurement.value.singleFilteredSamples.lastOrNull()?.sequenceNumber ?: -1) + 1
                        )
                    )
                    val angularValue: Float
                    if (_lastAngularSample == null) {
                        angularValue = linearValue
                        _lastAngularSample = angularValue
                        _lastGyroscopeTimeStamp = sensorData.second.timeStamp
                    } else {
                        angularValue = calculateElevationAngular(sensorData.second.xValue, sensorData.second.timeStamp)
                    }
                    val fusionFilteredValue = applyFusionFilter(linearValue, angularValue)
                    _measurement.value = _measurement.value.copy(
                        fusionFilteredSamples = _measurement.value.fusionFilteredSamples + Sample(
                            value = fusionFilteredValue,
                            sequenceNumber = (_measurement.value.fusionFilteredSamples.lastOrNull()?.sequenceNumber ?: -1) + 1
                        )
                    )
                    //Log.d("MeasurementService", "Linear timestamp: " + linearTimeStamp + " Angular timestamp: " + angularTimeStamp)
                }
        }

        measurementScope.launch {
            _polarSensorRepository.accelerometerData
                .filter { it.timeStamp >= 0 }
                .zip(_polarSensorRepository.gyroscopeData
                    .filter { it.timeStamp >= 0 }) { accelerometerData, gyroscopeData ->
                Pair(accelerometerData, gyroscopeData)
                }.collect { sensorData ->
                    //Log.d("MeasurementService","Linear values: " + sensorData.first.xValue + " " + sensorData.first.yValue + " " + sensorData.first.zValue)
                    //Log.d("MeasurementService","Angular values: " + sensorData.second.xValue + " " + sensorData.second.yValue + " " + sensorData.second.zValue)

                    val linearValue = calculateElevationAccelerometer(
                        sensorData.first.yValue,
                        sensorData.first.zValue
                    )
                    val singleFilteredValue = applySingleFilter(linearValue)

                    _measurement.value = _measurement.value.copy(
                        singleFilteredSamples = _measurement.value.singleFilteredSamples + Sample(
                            value = singleFilteredValue,
                            sequenceNumber = (_measurement.value.singleFilteredSamples.lastOrNull()?.sequenceNumber ?: -1) + 1
                        )
                    )
                    val angularValue: Float
                    if (_lastAngularSample == null) {
                        angularValue = linearValue
                        _lastAngularSample = angularValue
                        _lastGyroscopeTimeStamp = sensorData.second.timeStamp
                    } else {
                        angularValue = calculateElevationAngular(sensorData.second.xValue, sensorData.second.timeStamp)
                    }
                    val fusionFilteredValue = applyFusionFilter(linearValue, angularValue)
                    _measurement.value = _measurement.value.copy(
                        fusionFilteredSamples = _measurement.value.fusionFilteredSamples + Sample(
                            value = fusionFilteredValue,
                            sequenceNumber = (_measurement.value.fusionFilteredSamples.lastOrNull()?.sequenceNumber ?: -1) + 1
                        )
                    )
                    //Log.d("MeasurementService", "Linear timestamp: " + linearTimeStamp + " Angular timestamp: " + angularTimeStamp)
                }
        }
    }

    private fun calculateElevationAccelerometer(yValue: Float, zValue: Float) : Float {
        val angle = Math.toDegrees(atan2(zValue, yValue).toDouble()).toFloat()
        //Log.d("MeasurementService", "angle: " + angleDegrees)
        return angle
    }

    private fun calculateElevationAngular(xValue: Float, timeStamp: Long ) : Float {
        val deltaTime = (timeStamp - _lastGyroscopeTimeStamp) / 10.0f.pow(9)
        _lastGyroscopeTimeStamp = timeStamp
        var angle = Math.toDegrees((-xValue * deltaTime).toDouble()).toFloat()
        //Log.d("MeasurementService", "Difference in angle: " + angle)
        //Log.d("MeasurementService", "Last angle: " + _lastAngularSample)
        _lastAngularSample?.let {
            angle += it
        }
        _lastAngularSample = angle
        //Log.d("MeasurementService", "New angle: " + angle)
        return angle
    }

    private fun applySingleFilter(linearValue : Float) : Float {
        val filterFactor = 0.1f
        var singleFilteredValue = linearValue
        if (_measurement.value.singleFilteredSamples.isNotEmpty()) {
            singleFilteredValue = filterFactor * linearValue + (1 - filterFactor) * _measurement.value.singleFilteredSamples.last().value
            //Log.d("MeasurementService", "linear sample: " + linearSample + " last linear sample: " + _measurement.value.linearFilteredSamples.last() + " filtered sample: " + linearFilteredSample)
        }
        return singleFilteredValue
    }

    private fun applyFusionFilter(linearValue: Float, angularValue: Float) : Float {
        val filterFactor = 0.1f
        val fusionFilteredValue = filterFactor * linearValue + (1 - filterFactor) * angularValue
        //Log.d("MeasurementService", "linear sample: " + linearSample + " angular sample: " + angularSample + " result: " + fusionFilteredSample)
        return fusionFilteredValue
    }

    suspend fun saveRecording(measurement: Measurement) : Boolean {
        return _measurementDbRepository.insertMeasurement(measurement)
    }

    suspend fun getMeasurementsHistory() : List<Measurement> {
        return _measurementDbRepository.getMeasurements()
    }

    fun startInternalRecording() : Boolean {
        _measurement.value = Measurement()
        _lastAngularSample = null
        _lastGyroscopeTimeStamp = -1
        return  _internalSensorRepository.startListening()
    }

    suspend fun stopInternalRecording(measurement: Measurement) {
        _internalSensorRepository.stopListening()
    }

    fun hasRequiredBluetoothPermissions(): Boolean {
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
    }

    fun requestBluetoothPermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
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
        _lastGyroscopeTimeStamp = -1
        _polarSensorRepository.startAccStreaming(deviceId)
        _polarSensorRepository.startGyroStreaming(deviceId)
    }

    suspend fun stopPolarRecording(measurement: Measurement) {
        _polarSensorRepository.stopStreaming()
    }

    fun disconnectFromPolarDevice(deviceId: String) {
        _polarSensorRepository.disconnectFromDevice(deviceId)
    }

    fun exportMeasurement(measurement: Measurement) : Boolean {
        var csvContent = "Linear, Fusion\n"
        for (i in measurement.singleFilteredSamples.indices) {
            csvContent += measurement.singleFilteredSamples[i].toString() + ", " + measurement.fusionFilteredSamples[i].toString() + "\n"
        }
        return _measurementFileRepository.exportCsvToDownloads("ElevationAngle" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")), csvContent)
    }

    fun clearDb() {
        _measurementDbRepository.clearDb()
    }
}