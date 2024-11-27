package com.example.bluetoothapp.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    fun searchForDevices() {
        viewModelScope.launch {
            measurement.value.searchForDevices()
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