package com.example.bluetoothapp.model

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import com.example.bluetoothapp.data.InternalSensorRepository
import java.time.LocalDateTime

class Measurement (
    private var id : Int,
    private var _measured : LocalDateTime = LocalDateTime.now(),
    private var _linearFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _fusionFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _applicationContext : Context,
    private var _finished: Boolean = false
){
    val measured: LocalDateTime
        get() = _measured

    val linearFilteredSamples: MutableList<Float>
        get() = _linearFilteredSamples

    val fusionFilteredSamples : MutableList<Float>
        get() = _fusionFilteredSamples

    val finished: Boolean
        get() = _finished

    private val internalSensorRepository : InternalSensorRepository = InternalSensorRepository(_applicationContext)

    init {
        var currentLinearSample : Float = -1f
        var currentAngularSample : Float = -1f
        val sampleMediator = MediatorLiveData<Pair<Float, Float>>()
        sampleMediator.addSource(internalSensorRepository.linearAccelerationData) {
            val linearSample = calculateElevationLinear(it[1], it[2])
            applyLinearFilter(linearSample)
            if (currentAngularSample >= 0) {
                sampleMediator.value = Pair(linearSample, currentLinearSample)
                currentAngularSample = -1f
            }
            else {
                currentLinearSample = linearSample
            }
        }
       sampleMediator.addSource(internalSensorRepository.gyroscopeData) {
            val angularSample = calculateElevationAngular()
           if (currentLinearSample >= 0) {
               sampleMediator.value = Pair(currentLinearSample, angularSample)
               currentLinearSample = -1f
           }
           else {
               currentAngularSample = angularSample
           }
       }
        sampleMediator.observeForever( {
            applyFusionFilter(it.first, it.second)
        })
    }

    fun startRecording() {
        internalSensorRepository.startListening()
    }

    fun stopRecording() {
        internalSensorRepository.stopListening()
        _finished = true
    }

    private fun calculateElevationLinear(yValue: Float, zValue: Float) : Float {
        TODO()
    }

    private fun calculateElevationAngular() : Float {
        TODO()
    }

    private fun applyLinearFilter(linearSample : Float) {
        TODO()
    }

    private fun applyFusionFilter(linearSample: Float, angularSample: Float) {
        TODO()
    }
}