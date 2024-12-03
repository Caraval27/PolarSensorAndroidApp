package com.example.bluetoothapp.infrastructure

import android.content.Context
import android.util.Log
import com.example.bluetoothapp.domain.Device
import com.example.bluetoothapp.domain.SensorData
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarGyroData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.reactive.asFlow

class PolarSensorRepository(applicationContext: Context) {

    private val api: PolarBleApi by lazy {
        PolarBleApiDefaultImpl.defaultImplementation(
            applicationContext,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
            )
        )
    }

    private var accDisposable: Disposable? = null
    private var gyrDisposable: Disposable? = null

    private val _connectedDevices = MutableStateFlow(mutableSetOf<String>())
    val connectedDevices: StateFlow<Set<String>>
        get() = _connectedDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow("")
    val connectedDevice: StateFlow<String> = _connectedDevice.asStateFlow()

    private val _gyroscopeData = MutableStateFlow(SensorData())
    val gyroscopeData: StateFlow<SensorData>
        get() = _gyroscopeData

    private val _accelerometerData = MutableStateFlow(SensorData())
    val accelerometerData: StateFlow<SensorData>
        get() = _accelerometerData

    init {
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Device connected: ${polarDeviceInfo.deviceId}")
                _connectedDevices.value = _connectedDevices.value.toMutableSet().apply {
                    add(polarDeviceInfo.deviceId)
                }
                _connectedDevice.value = polarDeviceInfo.deviceId
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Discovered device: ${polarDeviceInfo.name}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Device disconnected: ${polarDeviceInfo.deviceId}")
                _connectedDevices.value = _connectedDevices.value.toMutableSet().apply {
                    remove(polarDeviceInfo.deviceId)
                }
                _connectedDevice.value = ""
            }
        })
    }

    fun searchForDevices() : Flow<Device> {
        Log.d("PolarSensorRepository", "Starting BLE device scan")
        return api.searchForDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { device -> Log.d("PolarSensorRepository", "Device found: ${device.deviceId}") }
            .map { polarDeviceInfo ->
                Device(
                    deviceId = polarDeviceInfo.deviceId,
                    name = polarDeviceInfo.name,
                    isConnectable = polarDeviceInfo.isConnectable
                )
            }.asFlow()
    }

    fun isDeviceConnected(deviceId: String) : Boolean {
        Log.d("PolarSensorRepository", "Device in list: ${_connectedDevices.value.contains(deviceId)}")
        return _connectedDevices.value.contains(deviceId)
    }

    fun connectToDevice(deviceId: String) {
        try {
            api.connectToDevice(deviceId)
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            Log.e("PolarSensorRepository", "Failed to connect. Reason $polarInvalidArgument ")
        }
    }

    fun startAccStreaming(deviceId: String) {
        if (_connectedDevice.value != deviceId) {
            Log.e("PolarSensorRepository", "Device is not connected: $deviceId")
            return
        }

        accDisposable = startStreaming<PolarAccelerometerData>(
            deviceId = deviceId,
            dataType = PolarBleApi.PolarDeviceDataType.ACC,
            dataCallback = { data ->
                data.samples.forEach { sample ->
                    _accelerometerData.value = SensorData(
                        xValue = sample.x.toFloat(),
                        yValue = sample.y.toFloat(),
                        zValue = sample.z.toFloat(),
                        timeStamp = sample.timeStamp
                    )
                }
            },
            errorCallback = { error ->
                Log.e("PolarSensorRepository", "Error streaming ACC data: ${error.message}")
            }
        )
    }

    fun startGyroStreaming(deviceId: String) {
        gyrDisposable = startStreaming<PolarGyroData>(
            deviceId = deviceId,
            dataType = PolarBleApi.PolarDeviceDataType.GYRO,
            dataCallback = { data ->
                data.samples.forEach { sample ->
                    _gyroscopeData.value = SensorData(
                        xValue = sample.x,
                        yValue = sample.y,
                        zValue = sample.z,
                        timeStamp = sample.timeStamp
                    )
                }
            },
            errorCallback = { error ->
                Log.e("PolarSensorRepository", "Error streaming GYRO data: ${error.message}")
            }
        )
    }

    private fun <T> startStreaming(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType,
        dataCallback: (T) -> Unit,
        errorCallback: (Throwable) -> Unit
    ): Disposable? {
        val startTime = System.currentTimeMillis()
        Log.d("PolarSensorRepository", "Streaming started at: $startTime")

        if (_connectedDevice.value != deviceId) {
            Log.e("PolarSensorRepository", "Device is not connected: $deviceId")
            return null
        }

        return api.requestStreamSettings(deviceId, dataType)
            .doOnSubscribe { Log.d("PolarSensorRepository", "Settings requested at: ${System.currentTimeMillis() - startTime}ms") }
            .flatMapPublisher { availableSettings ->
                Log.d("PolarSensorRepository", "Settings received at: ${System.currentTimeMillis() - startTime}ms")
                Log.d("PolarSensorRepository", "Available settings: ${availableSettings.settings}")

                val desiredSettings = availableSettings.maxSettings()

                Log.d("PolarSensorRepository", "Desired settings: ${desiredSettings.settings}")

                when (dataType) {
                    PolarBleApi.PolarDeviceDataType.ACC -> api.startAccStreaming(deviceId, desiredSettings)
                    PolarBleApi.PolarDeviceDataType.GYRO -> api.startGyroStreaming(deviceId, desiredSettings)
                    else -> throw IllegalArgumentException("Unsupported data type: $dataType")
                }
            }
            .doOnSubscribe { Log.d("PolarSensorRepository", "Streaming started for $dataType") }
            .doOnNext { Log.d("PolarSensorRepository", "Received data for $dataType") }
            //.debounce(MeasurementService.SENSOR_DELAY.toLong(), TimeUnit.MICROSECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { data -> Log.d("PolarSensorRepository", "First data received at: ${System.currentTimeMillis() - startTime}ms") }
            .subscribe(
                { data -> dataCallback(data as T) },
                { error -> errorCallback(error) }
            )
    }

    fun stopStreaming() {
        accDisposable?.dispose()
        gyrDisposable?.dispose()
    }

    fun disconnectFromDevice(deviceId: String) {
        try {
            if (_connectedDevice.value == deviceId) {
                api.disconnectFromDevice(deviceId)
                Log.d("PolarSensorRepository", "Disconnected from device: $deviceId")
                _connectedDevices.value = _connectedDevices.value.toMutableSet().apply {
                    remove(deviceId)
                }
                _connectedDevice.value = ""
            }
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            Log.e("PolarSensorRepository", "Failed to disconnect. Reason $polarInvalidArgument ")
        }
    }
}