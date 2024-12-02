package com.example.bluetoothapp.presentation.viewModel

import android.app.Application
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothapp.application.MeasurementService
import com.example.bluetoothapp.domain.Device
import com.example.bluetoothapp.domain.Measurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MeasurementVM(
    application: Application
) : AndroidViewModel(application) {
    private val _measurementService = MeasurementService(_applicationContext = application.applicationContext)

    private val _measurement = MutableStateFlow(Measurement())
    val measurement: StateFlow<Measurement>
        get() = _measurement

    private val _measurementHistory = MutableStateFlow(mutableListOf<Measurement>())
    val measurementHistory: StateFlow<MutableList<Measurement>>
        get() = _measurementHistory

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>>
        get() = _devices

    private val _isDeviceConnected = MutableStateFlow(false)
    val isDeviceConnected: StateFlow<Boolean> = _isDeviceConnected

    val connectedDevice: StateFlow<String> = _measurementService.connectedDevice

    private val _measurementState = MutableStateFlow(MeasurementState())
    val measurementState: StateFlow<MeasurementState>
        get() = _measurementState

    init {
        viewModelScope.launch {
            _measurementService.measurement.collect { newMeasurement ->
                _measurement.value = _measurement.value.copy(
                    linearFilteredSamples = newMeasurement.linearFilteredSamples,
                    fusionFilteredSamples = newMeasurement.fusionFilteredSamples
                )
            }
        }
    }

    fun hasRequiredBluetoothPermissions() : Boolean {
        return _measurementService.hasRequiredBluetoothPermissions()
    }

    fun requestPermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
        _measurementService.requestPermissions(requestPermissionLauncher)
    }

    fun isLocationEnabled() : Boolean {
        return _measurementService.isLocationEnabled()
    }

    fun searchForDevices() {
        viewModelScope.launch {
            _measurementService.searchForDevices()
                .collect { device ->
                    if (!_devices.value.contains(device))
                        _devices.value += device
                }
            Log.d("MeasurementVM", devices.value.last().deviceId)
        }
    }

    fun isDeviceConnected() {
        viewModelScope.launch {
            _isDeviceConnected.value = _measurementService.isDeviceConnected(_measurementState.value.chosenDeviceId)
        }
    }

    fun connectToDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                _measurementService.connectToPolarDevice(deviceId)
                _measurementState.value = _measurementState.value.copy(chosenDeviceId = deviceId)
            } catch (e: Exception) {
                Log.e("MeasurementVM", "Error connecting to device: ${e.message}")
            }
            Log.d("MeasurementVM", "After connecting is done")
            _isDeviceConnected.value = _measurementService.isDeviceConnected(_measurementState.value.chosenDeviceId)
        }
    }

    fun disconnectFromDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                _measurementService.disconnectFromPolarDevice(deviceId)
                _measurementState.value = _measurementState.value.copy(chosenDeviceId = "")
            } catch (e: Exception) {
                Log.e("MeasurementVM", "Error disconnecting from device: ${e.message}")
            }
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            when (_measurementState.value.sensorType) {
                SensorType.Polar -> _measurementService.startPolarRecording(_measurementState.value.chosenDeviceId)
                SensorType.Internal -> _measurementService.startInternalRecording()
            }
            _measurement.value = _measurement.value.copy(_timeMeasured = LocalDateTime.now())
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            when (_measurementState.value.sensorType) {
                SensorType.Polar -> _measurementService.stopPolarRecording(_measurement.value)
                SensorType.Internal -> _measurementService.stopInternalRecording(_measurement.value)
            }
            _measurementState.value = _measurementState.value.copy(ongoing = false)
        }
    }

    fun exportMeasurement() {
        viewModelScope.launch {
            val exported = _measurementService.exportMeasurement(_measurement.value)
            _measurementState.value = _measurementState.value.copy(exported = exported)
        }
    }

    fun getMeasurementHistory() {
        viewModelScope.launch {
            _measurementHistory.value = _measurementService.getMeasurementsHistory()
        }
    }

    fun setMeasurement(measurement: Measurement) {
        _measurement.value = measurement
    }

    fun setSensorType(sensorType: SensorType) {
        _measurementState.value = _measurementState.value.copy(sensorType = sensorType)
    }

    fun setOngoing(ongoing: Boolean) {
        _measurementState.value = _measurementState.value.copy(ongoing = ongoing)
    }

    fun setExported(exported: Boolean?) {
        _measurementState.value = _measurementState.value.copy(exported = exported)
    }

    fun clearDb() {
        CoroutineScope(Dispatchers.IO).launch {
            _measurementService.clearDb()
        }
    }
}

enum class SensorType {
    Polar,
    Internal
}

data class MeasurementState(
    val sensorType: SensorType = SensorType.Internal,
    val chosenDeviceId: String = "",
    val ongoing: Boolean = false,
    val exported: Boolean? = null
)