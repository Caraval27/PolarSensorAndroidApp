package com.example.bluetoothapp.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothapp.model.Device
import com.example.bluetoothapp.model.Measurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeasurementVM(
    application: Application
) : AndroidViewModel(application) {

    private val _measurement = MutableStateFlow(Measurement(_applicationContext = application.applicationContext))
    val measurement: StateFlow<Measurement>
        get() = _measurement.asStateFlow()

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>>
        get() = _devices.asStateFlow()

    fun hasRequiredPermissions() : Boolean {
        return measurement.value.hasRequiredPermissions()
    }

    fun searchForDevices() {
        viewModelScope.launch {
            measurement.value.searchForDevices()
                .subscribe ({ deviceList ->
                    _devices.value = deviceList
                }, { error ->
                    Log.e("MeasurementVM", "Error searching devices: ${error.message}")
                })
        }
    }

    fun connectToDevice(deviceId: String) {
        viewModelScope.launch {
            measurement.value.connectToPolarDevice(deviceId)
        }
    }

    fun startStreaming(deviceId: String) {
        viewModelScope.launch {
            measurement.value.startStreaming(deviceId)
        }
    }

    fun stopStreaming(deviceId: String) {
        viewModelScope.launch {
            measurement.value.stopStreaming(deviceId)
        }
    }
}