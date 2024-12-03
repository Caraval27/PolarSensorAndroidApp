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

    private val _connectedDevice = MutableStateFlow("")
    val connectedDevice: StateFlow<String> = _connectedDevice.asStateFlow()

    private val _accelerometerDataList = MutableStateFlow<List<SensorData>>(emptyList())
    val accelerometerDataList: StateFlow<List<SensorData>>
        get() = _accelerometerDataList

    private val _gyroscopeDataList = MutableStateFlow<List<SensorData>>(emptyList())
    val gyroscopeDataList: StateFlow<List<SensorData>>
        get() = _gyroscopeDataList

    init {
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Device connected: ${polarDeviceInfo.deviceId}")
                _connectedDevice.value = polarDeviceInfo.deviceId
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Discovered device: ${polarDeviceInfo.name}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Device disconnected: ${polarDeviceInfo.deviceId}")
                _connectedDevice.value = ""
            }
        })
    }

    fun searchForDevices() : Flow<Device> {
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

    fun connectToDevice(deviceId: String) {
        try {
            api.connectToDevice(deviceId)
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            Log.e("PolarSensorRepository", "Failed to connect. Reason $polarInvalidArgument ")
        }
    }

    fun startAccStreaming(deviceId: String) {
        accDisposable = startStreaming<PolarAccelerometerData>(
            deviceId = deviceId,
            dataType = PolarBleApi.PolarDeviceDataType.ACC,
            dataCallback = { data ->
                val accDataList = mutableListOf<SensorData>()
                data.samples.forEach { sample ->
                    accDataList.add(
                        SensorData(
                        xValue = sample.x.toFloat(),
                        yValue = sample.y.toFloat(),
                        zValue = sample.z.toFloat(),
                        timeStamp = sample.timeStamp
                        )
                    )
                }
                _accelerometerDataList.value = accDataList.toList()
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
                val gyroDataList = mutableListOf<SensorData>()
                data.samples.forEach { sample ->
                    gyroDataList.add(
                        SensorData(
                            xValue = sample.x,
                            yValue = sample.y,
                            zValue = sample.z,
                            timeStamp = sample.timeStamp
                        )
                    )
                }
                _gyroscopeDataList.value = gyroDataList.toList()
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
        if (_connectedDevice.value != deviceId) {
            Log.e("PolarSensorRepository", "Device is not connected: $deviceId")
            return null
        }

        return api.requestStreamSettings(deviceId, dataType)
            .flatMapPublisher { availableSettings ->
                val desiredSettings = availableSettings.maxSettings()

                when (dataType) {
                    PolarBleApi.PolarDeviceDataType.GYRO -> api.startGyroStreaming(deviceId, desiredSettings)
                    PolarBleApi.PolarDeviceDataType.ACC -> api.startAccStreaming(deviceId, desiredSettings)
                    else -> throw IllegalArgumentException("Unsupported data type: $dataType")
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
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
                _connectedDevice.value = ""
            }
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            Log.e("PolarSensorRepository", "Failed to disconnect. Reason $polarInvalidArgument ")
        }
    }
}