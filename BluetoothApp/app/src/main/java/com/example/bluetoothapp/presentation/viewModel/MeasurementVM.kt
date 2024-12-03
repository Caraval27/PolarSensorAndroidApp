package com.example.bluetoothapp.presentation.viewModel

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.provider.Settings
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

    private val _measurementHistory = MutableStateFlow<List<Measurement>>(emptyList())
    val measurementHistory: StateFlow<List<Measurement>>
        get() = _measurementHistory

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>>
        get() = _devices

    val connectedDevice: StateFlow<String> = _measurementService.connectedDevice

    private val _measurementState = MutableStateFlow(MeasurementState())
    val measurementState: StateFlow<MeasurementState>
        get() = _measurementState

    init {
        viewModelScope.launch {
            _measurementService.measurement.collect { newMeasurement ->
                if (_measurementState.value.recordingState == RecordingState.Requested) {
                    _measurementState.value = _measurementState.value.copy(recordingState = RecordingState.Ongoing)
                }
                _measurement.value = _measurement.value.copy(
                    singleFilteredSamples = newMeasurement.singleFilteredSamples,
                    fusionFilteredSamples = newMeasurement.fusionFilteredSamples
                )
            }
        }
    }

    fun bluetoothPermissions(
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
        activity: Activity?
    ) {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        viewModelScope.launch {
            var hasRequiredBluetoothPermissions = _measurementService.hasRequiredBluetoothPermissions()
            if (!hasRequiredBluetoothPermissions) {
                _measurementService.requestBluetoothPermissions(requestPermissionLauncher)
                kotlinx.coroutines.delay(2000)
                hasRequiredBluetoothPermissions = _measurementService.hasRequiredBluetoothPermissions()
            }

            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                try {
                    activity?.startActivityForResult(enableBtIntent, 1)
                } catch (e: SecurityException) {
                    Log.e("MeasurementVM", "Bluetooth enable request failed: ${e.message}")
                }
            }

            var isLocationEnabled = _measurementService.isLocationEnabled()
            if (!isLocationEnabled) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity?.startActivity(intent)
                isLocationEnabled = _measurementService.isLocationEnabled()
            }

            setPermissionsGranted(
                hasRequiredBluetoothPermissions &&
                    (bluetoothAdapter == null || bluetoothAdapter.isEnabled) &&
                    isLocationEnabled
            )
        }
    }

    fun searchForDevices() {
        viewModelScope.launch {
            _measurementService.searchForDevices()
                .collect { device ->
                    if (device.isConnectable && !_devices.value.contains(device))
                        _devices.value += device
                }
            Log.d("MeasurementVM", devices.value.last().deviceId)
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
            _measurement.value = _measurement.value.copy(_timeMeasured = LocalDateTime.now()) }
    }

    fun stopRecording() {
        viewModelScope.launch {
            when (_measurementState.value.sensorType) {
                SensorType.Polar -> _measurementService.stopPolarRecording(_measurement.value)
                SensorType.Internal -> _measurementService.stopInternalRecording(_measurement.value)
            }
        }
    }

    fun saveRecording() {
        _measurementState.value = _measurementState.value.copy(recordingState = RecordingState.Done)
        viewModelScope.launch {
            val saved = _measurementService.saveRecording(_measurement.value)
            _measurementState.value = _measurementState.value.copy(saved = saved)
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

    fun setRecordingState(recordingState: RecordingState) {
        _measurementState.value = _measurementState.value.copy(recordingState = recordingState)
    }

    fun setExported(exported: Boolean?) {
        _measurementState.value = _measurementState.value.copy(exported = exported)
    }

    fun setPermissionsGranted(permissionsGranted: Boolean?) {
        _measurementState.value = _measurementState.value.copy(permissionsGranted = permissionsGranted)
    }

    fun setSaved(saved: Boolean?) {
        _measurementState.value = _measurementState.value.copy(saved = saved)
    }

    fun clearDb() {
        CoroutineScope(Dispatchers.IO).launch {
            _measurementService.clearDb()
        }
    }
}

enum class RecordingState {
    Requested,
    Ongoing,
    Done
}

enum class SensorType {
    Polar,
    Internal
}

data class MeasurementState(
    val sensorType: SensorType = SensorType.Internal,
    val chosenDeviceId: String = "",
    val recordingState: RecordingState = RecordingState.Requested,
    val exported: Boolean? = null,
    val permissionsGranted: Boolean? = null,
    val saved: Boolean? = null
)