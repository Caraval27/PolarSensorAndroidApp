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
import com.example.bluetoothapp.domain.SensorType
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
                if (_measurementState.value.recordingState == RecordingState.Requested &&
                    (newMeasurement.singleFilteredSamples.isNotEmpty() || newMeasurement.fusionFilteredSamples.isNotEmpty())) {
                    _measurementState.value = _measurementState.value.copy(recordingState = RecordingState.Ongoing)
                }
                _measurement.value = _measurement.value.copy(
                    singleFilteredSamples = newMeasurement.singleFilteredSamples,
                    fusionFilteredSamples = newMeasurement.fusionFilteredSamples
                )
            }
        }
    }

    fun checkBluetoothPermissions(
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
        activity: Activity
    ) {
        viewModelScope.launch {
            val hasRequiredBluetoothPermissions = _measurementService.hasRequiredBluetoothPermissions()
            if (!hasRequiredBluetoothPermissions) {
                _measurementService.requestBluetoothPermissions(requestPermissionLauncher)
            }
            else {
                checkBluetoothEnabled(true, activity)
            }
        }
    }

    fun checkBluetoothEnabled(hasBluetoothPermissions : Boolean, activity: Activity) {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            try {
                activity.startActivityForResult(enableBtIntent, 1)
            } catch (exception : SecurityException) {
                Log.d("MeasurementVM", "Exception occurred: ", exception)
            }
        }

        var isLocationEnabled = _measurementService.isLocationEnabled()
        if (!isLocationEnabled) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivity(intent)
            isLocationEnabled = _measurementService.isLocationEnabled()
        }

        setBluetoothAvailable(
            hasBluetoothPermissions &&
                    (bluetoothAdapter == null || bluetoothAdapter.isEnabled) &&
                    isLocationEnabled
        )
    }

    fun searchForDevices() {
        viewModelScope.launch {
            _measurementService.searchForDevices()
                .collect { device ->
                    if (device.isConnectable && !_devices.value.contains(device))
                        _devices.value += device
                }
        }
    }

    fun connectToDevice(deviceId: String) {
        viewModelScope.launch {
            _measurementService.connectToPolarDevice(deviceId)
            _measurementState.value = _measurementState.value.copy(chosenDeviceId = deviceId)
        }
    }

    fun disconnectFromDevice(deviceId: String) {
        viewModelScope.launch {
            _measurementService.disconnectFromPolarDevice(deviceId)
            _measurementState.value = _measurementState.value.copy(chosenDeviceId = "")
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            when (_measurementState.value.sensorType) {
                SensorType.Polar -> _measurementService.startPolarRecording(_measurementState.value.chosenDeviceId)
                SensorType.Internal -> _measurementService.startInternalRecording()
            }
            _measurement.value = _measurement.value.copy(
                timeMeasured = LocalDateTime.now(),
                sensorType = _measurementState.value.sensorType
            )
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            when (_measurementState.value.sensorType) {
                SensorType.Polar -> _measurementService.stopPolarRecording()
                SensorType.Internal -> _measurementService.stopInternalRecording()
            }
            setRecordingState(RecordingState.Done)
        }
    }

    fun saveRecording() {
        _measurementState.value = _measurementState.value.copy(recordingState = RecordingState.Done)
        stopRecording()
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

    fun setBluetoothAvailable(bluetoothAvailable: Boolean?) {
        _measurementState.value = _measurementState.value.copy(bluetoothAvailable = bluetoothAvailable)
    }

    fun setSaved(saved: Boolean?) {
        _measurementState.value = _measurementState.value.copy(saved = saved)
    }
}

enum class RecordingState {
    Requested,
    Ongoing,
    Done
}

data class MeasurementState(
    val sensorType: SensorType = SensorType.Internal,
    val chosenDeviceId: String = "",
    val recordingState: RecordingState = RecordingState.Requested,
    val exported: Boolean? = null,
    val bluetoothAvailable: Boolean? = null,
    val saved: Boolean? = null
)