package com.example.bluetoothapp.presentation.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothapp.application.MeasurementService
import com.example.bluetoothapp.domain.Device
import com.example.bluetoothapp.domain.Measurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeasurementVM(
    application: Application
) : AndroidViewModel(application) {
    private val _measurementService = MeasurementService(_applicationContext = application.applicationContext)

    private val _currentMeasurement = MutableStateFlow(Measurement())
    val currentMeasurement: StateFlow<Measurement>
        get() = _currentMeasurement

    private val _linearFilteredSamples = MutableStateFlow(emptyList<Float>())
    val linearFilteredSamples: StateFlow<List<Float>>
        get() = _linearFilteredSamples

    private val _fusionFilteredSamples = MutableStateFlow(emptyList<Float>())
    val fusionFilteredSamples: StateFlow<List<Float>>
        get () = _fusionFilteredSamples

    private val _measurementHistory = MutableStateFlow(mutableListOf<Measurement>())
    val measurementHistory: StateFlow<MutableList<Measurement>>
        get() = _measurementHistory

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>>
        get() = _devices

    private val _measurementState = MutableStateFlow(MeasurementState())
    val measurementState: StateFlow<MeasurementState>
        get() = _measurementState

    init {
        viewModelScope.launch {
            _measurementService.linearFilteredSamples.collect { newLinearFilteredSamples ->
                _linearFilteredSamples.value = newLinearFilteredSamples
            }
        }
        viewModelScope.launch {
            _measurementService.fusionFilteredSamples.collect { newFusionFilteredSamples ->
                _fusionFilteredSamples.value = newFusionFilteredSamples
            }
        }
    }

    fun hasRequiredPermissions() : Boolean {
        return _measurementService.hasRequiredPermissions()
    }

    fun searchForDevices() {
        viewModelScope.launch {
            _measurementService.searchForDevices()
                .subscribe ({ deviceList ->
                    _devices.value = deviceList
                }, { error ->
                    Log.e("MeasurementVM", "Error searching devices: ${error.message}")
                })
        }
    }

    fun connectToDevice(deviceId: String) {
        viewModelScope.launch {
            _measurementService.connectToPolarDevice(deviceId)
            _measurementState.value = _measurementState.value.copy(chosenDeviceId = deviceId)
        }
    }

    fun startRecording() {
        _linearFilteredSamples.value = emptyList()
        _fusionFilteredSamples.value = emptyList()
        viewModelScope.launch {
            when (_measurementState.value.sensorType) {
                SensorType.Polar -> _measurementService.startPolarRecording(_measurementState.value.chosenDeviceId)
                SensorType.Internal -> _measurementService.startInternalRecording()
            }
            _measurementState.value = _measurementState.value.copy(ongoing = true)
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            when (_measurementState.value.sensorType) {
                SensorType.Polar -> _measurementService.stopPolarRecording()
                SensorType.Internal -> _measurementService.stopInternalRecording()
            }
            _measurementState.value = _measurementState.value.copy(ongoing = false)
        }
    }

    fun getMeasurementHistory() {
        viewModelScope.launch {
            _measurementHistory.value = _measurementService.getMeasurementsHistory()
        }
    }

    fun setCurrentMeasurement(measurement: Measurement) {
        _currentMeasurement.value = measurement
    }

    fun setSensorType(sensorType: SensorType) {
        _measurementState.value = _measurementState.value.copy(sensorType = sensorType)
    }

    /*Ska raderas sen*/
    fun testInsert() {
        viewModelScope.launch {
            Log.d("HistoryScreen", "inserted")
            _measurementService.insertMeasurement(_measurementService.testInsert())
        }
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
    val ongoing: Boolean = true
)