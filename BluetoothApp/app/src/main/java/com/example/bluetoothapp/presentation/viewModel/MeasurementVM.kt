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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeasurementVM(
    application: Application
) : AndroidViewModel(application) {

    private val _currentMeasurement = MutableStateFlow(Measurement())
    val currentMeasurement: StateFlow<Measurement>
        get() = _currentMeasurement.asStateFlow()

    private val _measurementHistory = MutableStateFlow(mutableListOf<Measurement>())
    val measurementHistory: StateFlow<MutableList<Measurement>>
        get() = _measurementHistory.asStateFlow()

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>>
        get() = _devices.asStateFlow()

    private val _measurementService = MeasurementService(_applicationContext = application.applicationContext)

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
        }
    }

    fun startStreaming(deviceId: String) {
        viewModelScope.launch {
            _measurementService.startPolarRecording(deviceId)
        }
    }

    fun stopStreaming(deviceId: String) {
        viewModelScope.launch {
            _measurementService.stopPolarRecording(deviceId)
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