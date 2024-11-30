package com.example.bluetoothapp.infrastructure

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bluetoothapp.domain.Device
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private var scanDisposable: Disposable? = null
    private var autoConnectDisposable: Disposable? = null
    private var accDisposable: Disposable? = null
    private var gyrDisposable: Disposable? = null

    private val _devices = MutableStateFlow<List<PolarDeviceInfo>>(emptyList())
    val devices: StateFlow<List<PolarDeviceInfo>> = _devices

    private val _connectedDevices = MutableStateFlow(mutableSetOf<String>())
    val connectedDevices: StateFlow<Set<String>> = _connectedDevices.asStateFlow()

    private val _gyroscopeData = MutableStateFlow(floatArrayOf())
    val gyroscopeData: StateFlow<FloatArray>
        get() = _gyroscopeData.asStateFlow()

    private val _linearAccelerationData = MutableStateFlow(floatArrayOf())
    val linearAccelerationData: StateFlow<FloatArray>
        get() = _linearAccelerationData.asStateFlow()

    init {
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Device connected: ${polarDeviceInfo.deviceId}")
                _connectedDevices.value = _connectedDevices.value.toMutableSet().apply {
                    add(polarDeviceInfo.deviceId)
                }
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Discovered device: ${polarDeviceInfo.name}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Device disconnected: ${polarDeviceInfo.deviceId}")
                _connectedDevices.value = _connectedDevices.value.toMutableSet().apply {
                    remove(polarDeviceInfo.deviceId)
                }
            }
        })
    }

    fun searchForDevices() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("PolarSensorRepository", "Bluetooth is not enabled")
            return
        }

        Log.d("PolarSensorRepository", "Starting BLE device scan")
        val isDisposed = scanDisposable?.isDisposed ?: true
        if (isDisposed) {
            scanDisposable = api.searchForDevice()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarDeviceInfo: PolarDeviceInfo ->
                        _devices.value += polarDeviceInfo
                        Log.d("PolarSensorRepository", "Polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable)
                    }, { error: Throwable ->
                        Log.e("PolarSensorRepository", "Error searching for devices: ${error.message}")
                    }, {
                        Log.d("PolarSensorRepository", "Scan complete")
                    }
                )
        } else {
            scanDisposable?.dispose()
        }
        /*
        return api.searchForDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .map { polarDeviceInfo ->
                Device(
                    deviceId = polarDeviceInfo.deviceId,
                    name = polarDeviceInfo.name ?: "Unknown",
                    isConnectable = polarDeviceInfo.isConnectable
                )
            }
            .toList()

        return Single.create { emitter ->
            val discoveredDevices = mutableListOf<Device>()
            val disposable = api.searchForDevice()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { deviceInfo ->
                        val device = Device(
                            deviceId = deviceInfo.deviceId,
                            name = deviceInfo.name,
                            isConnectable = deviceInfo.isConnectable
                        )
                        discoveredDevices.add(device)
                        Log.d("PolarSensorRepository", "Device found: $device")
                    },
                    { error ->
                        Log.e("PolarSensorRepository", "Error searching for devices: ${error.message}")
                        emitter.onError(error)
                    },
                    {
                        emitter.onSuccess(discoveredDevices)
                    }
                )
            disposables.add(disposable)
        }

        return api.searchForDevice()
            .map { info ->
                Device(
                    deviceId = info.deviceId,
                    name = info.name,
                    isConnectable = info.isConnectable
                )
        }.toList()*/
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
        Log.d("PolarSensorRepository", "3")
        if (!isDeviceConnected(deviceId)) {
            Log.e("PolarSensorRepository", "Device is not connected: $deviceId")
            return
        }

        val isDisposed = accDisposable?.isDisposed ?: true
        if (isDisposed) {
            accDisposable = api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
                    .flatMapPublisher { settings: PolarSensorSetting ->
                        api.startAccStreaming(deviceId, settings)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { Log.d("ACC in Repository", "Starting stream for device: $deviceId") }
                    .subscribe(
                        { polarAccelerometerData: PolarAccelerometerData ->
                            if (polarAccelerometerData.samples.isNotEmpty()) {
                                polarAccelerometerData.samples.forEach { sample ->
                                    Log.d(
                                        "ACC in Repository",
                                        "ACC    x: ${sample.x} y: ${sample.y} z: ${sample.z} timeStamp: ${sample.timeStamp}"
                                    )
                                    _linearAccelerationData.value = (floatArrayOf(
                                        sample.x.toFloat(), sample.y.toFloat(), sample.z.toFloat()
                                    ))
                                }
                            } else {
                                Log.d("ACC in Repository", "No ACC samples received in this batch")
                            }
                        }, { error: Throwable ->
                            Log.e("ACC in Repository", "Error streaming ACC data: ${error.message}", error)
                        }, {
                            Log.d("ACC in Repository", "ACC stream complete")
                        }
                    )

        } else {
            Log.d("PolarSensorRepository", "Is not disposed")
            accDisposable?.dispose()
        }

        /*val isDisposed = accDisposable?.isDisposed ?: true
        if (isDisposed) {
            accDisposable = api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
                    .flatMapPublisher { settings: PolarSensorSetting ->
                        api.startAccStreaming(deviceId, settings)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ data ->
                        for (sample in data.samples) {
                            _linearAccelerationData.value = (floatArrayOf(
                                sample.x.toFloat(), sample.y.toFloat(), sample.z.toFloat()
                            ))
                        }
                    }, { error ->
                        Log.e(
                            "PolarSensorRepository",
                            "Error streaming ACC data: ${error.message}"
                        )
                    })

        } else {
            accDisposable?.dispose()
        }*/
    }

    fun startGyroStreaming(deviceId: String) {
        val isDisposed = gyrDisposable?.isDisposed ?: true
        if (isDisposed) {
            gyrDisposable =
                (api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.GYRO)
                    .flatMapPublisher { settings: PolarSensorSetting ->
                        api.startGyroStreaming(deviceId, settings)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { data ->
                            for (sample in data.samples) {
                                _gyroscopeData.value = (floatArrayOf(
                                    sample.x, sample.y, sample.z
                                ))
                            }
                        },
                        { error ->
                            Log.e(
                                "PolarSensorRepository",
                                "Error streaming GYRO data: ${error.message}"
                            )
                        }
                    )
                        )
        } else {
            gyrDisposable?.dispose()
        }
    }

    fun stopStreaming() {
        accDisposable?.dispose()
        gyrDisposable?.dispose()
    }

    fun autoConnectToDevice() {
        autoConnectDisposable?.dispose() // Dispose of any previous subscriptions
        autoConnectDisposable = api.autoConnectToDevice(-60, "180D", null)
            .subscribe(
                {
                    Log.d("PolarSensorRepository", "Auto-connect search complete.")
                },
                { error ->
                    Log.e("PolarSensorRepository", "Auto-connect failed: ${error.message}")
                }
            )
    }

    fun disconnectFromDevice(deviceId: String) {
        api.disconnectFromDevice(deviceId)
    }
}