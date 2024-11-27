package com.example.bluetoothapp.data

import android.content.Context
import android.util.Log
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApi.PolarDeviceDataType
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarGyroData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class PolarSensorManager(context: Context) {
    private val polarApi: PolarBleApi by lazy {
        PolarBleApiDefaultImpl.defaultImplementation(
            context.applicationContext,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_H10_EXERCISE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_LED_ANIMATION
            )
        )
    }

    private val disposables = CompositeDisposable()

    private val _accelerometerData = MutableSharedFlow<List<PolarAccelerometerData.PolarAccelerometerDataSample>>()
    val accelerometerData: SharedFlow<List<PolarAccelerometerData.PolarAccelerometerDataSample>> = _accelerometerData

    private val _gyroscopeData = MutableSharedFlow<List<PolarGyroData.PolarGyroDataSample>>()
    val gyroscopeData: SharedFlow<List<PolarGyroData.PolarGyroDataSample>> = _gyroscopeData

    init {
        setupPolarApi()
    }

    private fun setupPolarApi() {
        polarApi.setApiCallback(object : PolarBleApiCallback() {
            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSDK", "Connected to device: ${polarDeviceInfo.deviceId}")
                /*deviceId = polarDeviceInfo.deviceId
                deviceConnected = true
                val buttonText = getString(R.string.disconnect_from_device, deviceId)
                toggleButtonDown(connectButton, buttonText)*/
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSDK", "Disconnected from device: ${polarDeviceInfo.deviceId}")
                /*deviceConnected = false
                val buttonText = getString(R.string.connect_to_device, deviceId)
                toggleButtonUp(connectButton, buttonText)
                toggleButtonUp(toggleSdkModeButton, R.string.enable_sdk_mode)*/
            }
        })
    }

    fun connectToDevice(deviceId: String) {
        polarApi.connectToDevice(deviceId)
    }

    fun disconnectFromDevice(deviceId: String) {
        polarApi.disconnectFromDevice(deviceId)
    }

    fun shutDown() {
        disposables.clear()
        polarApi.shutDown()
    }

    fun startAccelerometerStreaming(deviceId: String) {
        val accDisposable = polarApi.requestStreamSettings(deviceId, PolarDeviceDataType.ACC)
            .flatMapPublisher { settings : PolarSensorSetting ->
                polarApi.startAccStreaming(deviceId, settings)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { polarAccelerometerData: PolarAccelerometerData ->
                    _accelerometerData.tryEmit(polarAccelerometerData.samples)
                },
                { error: Throwable ->
                    Log.e("PolarRepository", "ACC Stream Error: ${error.message}")
                }
            )
        disposables.add(accDisposable)
    }

    fun startGyroscopeStreaming(deviceId: String) {
        val gyroDisposable = polarApi.requestStreamSettings(deviceId, PolarDeviceDataType.GYRO)
            .flatMapPublisher { settings : PolarSensorSetting ->
                polarApi.startGyroStreaming(deviceId, settings)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { polarGyroData: PolarGyroData ->
                    _gyroscopeData.tryEmit(polarGyroData.samples)
                },
                { error: Throwable ->
                    Log.e("PolarRepository", "GYRO Stream Error: ${error.message}")
                }
            )
        disposables.add(gyroDisposable)
    }

    fun stopStreaming() {
        disposables.clear()
    }
}

/*package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.PolarSensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PolarSensorViewModel(private val repository: PolarSensorRepository) : ViewModel() {

    private val _accelerometerData = MutableStateFlow<List<String>>(emptyList())
    val accelerometerData: StateFlow<List<String>> = _accelerometerData

    private val _gyroscopeData = MutableStateFlow<List<String>>(emptyList())
    val gyroscopeData: StateFlow<List<String>> = _gyroscopeData

    fun connectToDevice(deviceId: String) {
        repository.connectToDevice(deviceId)
    }

    fun disconnectFromDevice(deviceId: String) {
        repository.disconnectFromDevice(deviceId)
    }

    fun startAccelerometerStreaming(deviceId: String) {
        viewModelScope.launch {
            repository.startAccelerometerStreaming(deviceId)
            repository.accelerometerData.collectLatest { data ->
                _accelerometerData.value = data.map { "X: ${it.x}, Y: ${it.y}, Z: ${it.z}" }
            }
        }
    }

    fun startGyroscopeStreaming(deviceId: String) {
        viewModelScope.launch {
            repository.startGyroscopeStreaming(deviceId)
            repository.gyroscopeData.collectLatest { data ->
                _gyroscopeData.value = data.map { "X: ${it.x}, Y: ${it.y}, Z: ${it.z}" }
            }
        }
    }

    fun stopStreaming() {
        repository.stopStreaming()
    }

    override fun onCleared() {
        super.onCleared()
        repository.shutDown()
    }
}
*/

/*private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                PERMISSION_REQUEST_CODE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
    }*/

/*package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.PolarSensorViewModel

@Composable
fun PolarSensorScreen(viewModel: PolarSensorViewModel, deviceId: String) {
    val accData by viewModel.accelerometerData.collectAsState()
    val gyroData by viewModel.gyroscopeData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Accelerometer Data:")
        accData.forEach { data -> Text(data) }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Gyroscope Data:")
        gyroData.forEach { data -> Text(data) }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.connectToDevice(deviceId) }) {
            Text("Connect to Device")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.startAccelerometerStreaming(deviceId) }) {
            Text("Start Accelerometer Streaming")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.startGyroscopeStreaming(deviceId) }) {
            Text("Start Gyroscope Streaming")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.disconnectFromDevice(deviceId) }) {
            Text("Disconnect from Device")
        }
    }
}
*/
