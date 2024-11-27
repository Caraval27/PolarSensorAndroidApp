package com.example.bluetoothapp.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bluetoothapp.model.Device
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable

class PolarSensorRepository(applicationContext: Context) {

    private val api: PolarBleApi = PolarBleApiDefaultImpl.defaultImplementation(
        applicationContext,
        setOf(
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
            PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
        )
    )

    private val disposables = CompositeDisposable()

    /*private val _devices = MutableStateFlow<List<PolarDeviceInfo>>(emptyList())
    val devices: StateFlow<List<PolarDeviceInfo>> = _devices*/

    private val _gyroscopeData = MutableLiveData<FloatArray>()
    val gyroscopeData: LiveData<FloatArray> get() = _gyroscopeData

    private val _linearAccelerationData = MutableLiveData<FloatArray>()
    val linearAccelerationData: LiveData<FloatArray> get() = _linearAccelerationData

    init {
        api.setApiCallback(object : PolarBleApiCallback() {
            /*fun deviceDiscovered(polarDeviceInfo: PolarDeviceInfo) {
                _devices.value += polarDeviceInfo
            }*/

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Device connected: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("PolarSensorRepository", "Device disconnected: ${polarDeviceInfo.deviceId}")
            }
        })
    }

    fun searchForDevices(): Single<List<Device>> {
        return api.searchForDevice()
            .map { info ->
                Device(
                    deviceId = info.deviceId,
                    name = info.name,
                    isConnectable = info.isConnectable
            )
        }.toList()
    }

    fun connectToDevice(deviceId: String) {
        api.connectToDevice(deviceId)
    }

    fun startAccStreaming(deviceId: String) {
         disposables.add(api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
             .flatMapPublisher { settings : PolarSensorSetting ->
                api.startAccStreaming(deviceId, settings)
             }
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe({ data ->
                 for (sample in data.samples) {
                     _linearAccelerationData.postValue(floatArrayOf(
                         sample.x.toFloat(), sample.y.toFloat(), sample.z.toFloat()
                     ))
                 }
             }, {
                 error -> Log.e("PolarSensorRepository", "Error streaming ACC data: ${error.message}")
             })
        )
    }

    fun startGyroStreaming(deviceId: String) {
        disposables.add(api.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.GYRO)
            .flatMapPublisher { settings : PolarSensorSetting ->
                api.startGyroStreaming(deviceId, settings)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                for (sample in data.samples) {
                    _gyroscopeData.postValue(floatArrayOf(
                        sample.x, sample.y, sample.z
                    ))
                }
            }, {
                error -> Log.e("PolarSensorRepository", "Error streaming GYRO data: ${error.message}") }
            )
        )
    }

    fun stopStreaming() {
        disposables.clear()
    }

    fun disconnectFromDevice(deviceId: String) {
        api.disconnectFromDevice(deviceId)
    }
}