package com.example.bluetoothapp.infrastructure

import android.bluetooth.BluetoothGatt
import android.content.Context
import android.util.Log
import com.example.bluetoothapp.domain.Device
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarGyroData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
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

    fun searchForDevices() : Flow<Device> {
        Log.d("PolarSensorRepository", "Starting BLE device scan")
        return api.searchForDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .map { polarDeviceInfo ->
                Device(
                    deviceId = polarDeviceInfo.deviceId,
                    name = polarDeviceInfo.name,
                    isConnectable = polarDeviceInfo.isConnectable
                )
            }.asFlow()
    }
    /*val isDisposed = scanDisposable?.isDisposed ?: true
   if (isDisposed) {
       scanDisposable = api.searchForDevice()
           .observeOn(AndroidSchedulers.mainThread())
           .subscribe(
               { polarDeviceInfo: PolarDeviceInfo ->
                   emit(
                       Device(
                           deviceId = polarDeviceInfo.deviceId,
                           name = polarDeviceInfo.name ?: "Unknown",
                           isConnectable = polarDeviceInfo.isConnectable
                       )
                   )
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

    fun isDeviceConnected(deviceId: String) : Boolean {
        Log.d("PolarSensorRepository", "Device in list: ${_connectedDevices.value.contains(deviceId)}")
        return _connectedDevices.value.contains(deviceId)
    }

    fun connectToDevice(deviceId: String) {
        try {
            api.connectToDevice(deviceId)
            //val gatt = gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            Log.e("PolarSensorRepository", "Failed to connect. Reason $polarInvalidArgument ")
        }
    }

    fun startAccStreaming(deviceId: String) {
        if (!isDeviceConnected(deviceId)) {
            Log.e("PolarSensorRepository", "Device is not connected: $deviceId")
            return
        }
        /*
                val isDisposed = accDisposable?.isDisposed ?: true
                if (isDisposed) {
                    accDisposable = api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
                        .doOnSubscribe { Log.d("PolarSensorRepository", "Settings requested at: ${System.currentTimeMillis() - startTime}ms") }
                        .flatMapPublisher { settings: PolarSensorSetting ->
                            Log.d("PolarSensorRepository", "Settings received at: ${System.currentTimeMillis() - startTime}ms")
                            api.startAccStreaming(deviceId, settings)
                        }
                        //.debounce(MeasurementService.SENSOR_DELAY.toLong(), TimeUnit.MILLISECONDS) // Emit data no more frequently than the delay
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext { data ->
                            Log.d("PolarSensorRepository", "First data received at: ${System.currentTimeMillis() - startTime}ms")
                        }
                        .subscribe({ data ->
                            if (data.samples.isNotEmpty()) {
                                val latestSample = data.samples.last()
                                _linearAccelerationData.value = floatArrayOf(
                                    latestSample.x.toFloat(),
                                    latestSample.y.toFloat(),
                                    latestSample.z.toFloat()
                                )
                            }
                        }, { error ->
                            Log.e("PolarSensorRepository", "Error streaming ACC data: ${error.message}")
                        })
                } else {
                    accDisposable?.dispose()
                }


                val isDisposed = accDisposable?.isDisposed ?: true
                if (isDisposed) {
                    accDisposable = api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
                        .flatMapPublisher { settings: PolarSensorSetting ->
                            api.startAccStreaming(deviceId, settings)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ data ->
                            if (data.samples.isNotEmpty()) {
                                val latestSample = data.samples.last()
                                _linearAccelerationData.value = floatArrayOf(
                                    latestSample.x.toFloat(),
                                    latestSample.y.toFloat(),
                                    latestSample.z.toFloat()
                                )
                            }
                        }, { error ->
                            Log.e(
                                "PolarSensorRepository",
                                "Error streaming ACC data: ${error.message}"
                            )
                        })

                } else {
                    accDisposable?.dispose()
                }
                for (sample in data.samples) {
                                _linearAccelerationData.value = (floatArrayOf(
                                    sample.x.toFloat(), sample.y.toFloat(), sample.z.toFloat()
                                ))
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
                }*/

        accDisposable = startStreaming<PolarAccelerometerData>(
            deviceId = deviceId,
            dataType = PolarBleApi.PolarDeviceDataType.ACC,
            dataCallback = { data ->
                data.samples.lastOrNull()?.let { sample ->
                    _linearAccelerationData.value = floatArrayOf(sample.x.toFloat(), sample.y.toFloat(), sample.z.toFloat())
                }
            },
            errorCallback = { error ->
                Log.e("PolarSensorRepository", "Error streaming ACC data: ${error.message}")
            }
        )
    }

    fun startGyroStreaming(deviceId: String) {
        /*val isDisposed = gyrDisposable?.isDisposed ?: true
        if (isDisposed) {
            gyrDisposable = api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.GYRO)
                .flatMapPublisher { settings: PolarSensorSetting ->
                    api.startGyroStreaming(deviceId, settings)
                }
                //.debounce(MeasurementService.SENSOR_DELAY.toLong(), TimeUnit.MILLISECONDS) // Emit data no more frequently than the delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                        if (data.samples.isNotEmpty()) {
                            val latestSample = data.samples.last()
                            _gyroscopeData.value = floatArrayOf(
                                latestSample.x.toFloat(),
                                latestSample.y.toFloat(),
                                latestSample.z.toFloat()
                            )
                        }
                    },
                    { error ->
                        Log.e(
                            "PolarSensorRepository",
                            "Error streaming GYRO data: ${error.message}"
                        )
                    }
                )
        } else {
            gyrDisposable?.dispose()
        }*/
        gyrDisposable = startStreaming<PolarGyroData>(
            deviceId = deviceId,
            dataType = PolarBleApi.PolarDeviceDataType.GYRO,
            dataCallback = { data ->
                data.samples.lastOrNull()?.let { sample ->
                    _gyroscopeData.value = floatArrayOf(sample.x, sample.y, sample.z)
                }
            },
            errorCallback = { error ->
                Log.e("PolarSensorRepository", "Error streaming GYRO data: ${error.message}")
            }
        )
    }

    fun <T> startStreaming(
        deviceId: String,
        dataType: PolarBleApi.PolarDeviceDataType,
        dataCallback: (T) -> Unit,
        errorCallback: (Throwable) -> Unit
    ): Disposable? {
        val startTime = System.currentTimeMillis()
        Log.d("PolarSensorRepository", "Streaming started at: $startTime")

        if (!isDeviceConnected(deviceId)) {
            Log.e("PolarSensorRepository", "Device is not connected: $deviceId")
            return null
        }

        return api.requestStreamSettings(deviceId, dataType)
            .doOnSubscribe { Log.d("PolarSensorRepository", "Settings requested at: ${System.currentTimeMillis() - startTime}ms") }
            .flatMapPublisher { availableSettings ->
                Log.d("PolarSensorRepository", "Settings received at: ${System.currentTimeMillis() - startTime}ms")
                Log.d("PolarSensorRepository", "Available settings: ${availableSettings.settings}")

                val supportedSampleRates = availableSettings.settings[PolarSensorSetting.SettingType.SAMPLE_RATE]
                val supportedRanges = availableSettings.settings[PolarSensorSetting.SettingType.RANGE]

                val maxSampleRate = supportedSampleRates?.maxOrNull() ?: 100
                val selectedRange = supportedRanges?.firstOrNull() ?: 8

                val desiredSettingsMap = hashMapOf<PolarSensorSetting.SettingType, Int>(
                    PolarSensorSetting.SettingType.SAMPLE_RATE to maxSampleRate,
                    PolarSensorSetting.SettingType.RANGE to selectedRange
                )

                val desiredSettings = PolarSensorSetting(desiredSettingsMap)

                //val desiredSettings = settings.maxSettings()

                Log.d("PolarSensorRepository", "Desired settings: ${desiredSettings.settings}")

                when (dataType) {
                    PolarBleApi.PolarDeviceDataType.ACC -> api.startAccStreaming(deviceId, desiredSettings)
                    PolarBleApi.PolarDeviceDataType.GYRO -> api.startGyroStreaming(deviceId, desiredSettings)
                    else -> throw IllegalArgumentException("Unsupported data type: $dataType")
                }
            }
            .doOnSubscribe { Log.d("PolarSensorRepository", "Streaming started for $dataType") }
            .doOnNext { Log.d("PolarSensorRepository", "Received data for $dataType") }
            //.observeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.computation())
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
            if (_connectedDevices.value.contains(deviceId)) {
                api.disconnectFromDevice(deviceId)
                Log.d("PolarSensorRepository", "Disconnected from device: $deviceId")
            }
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            Log.e("PolarSensorRepository", "Failed to disconnect. Reason $polarInvalidArgument ")
        }
    }
}