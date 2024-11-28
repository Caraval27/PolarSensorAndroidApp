package com.example.bluetoothapp.domain

import java.time.LocalDateTime

class Measurement (
    private var _id : Int = 0,
    private var _measured : LocalDateTime = LocalDateTime.now(),
    private var _linearFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _fusionFilteredSamples : MutableList<Float> = mutableListOf(),
    private var _lastAngularSample: Float = -1f,
    private var _finished: Boolean = false
){

    val id: Int
        get() = _id

    val measured: LocalDateTime
        get() = _measured

    val linearFilteredSamples: MutableList<Float>
        get() = _linearFilteredSamples

    val fusionFilteredSamples : MutableList<Float>
        get() = _fusionFilteredSamples

    var finished: Boolean
        get() = _finished
        set(value) {
            _finished = value
        }

    var lastAngularSample: Float
        get() = _lastAngularSample
        set(value) {
            _lastAngularSample = value
        }

    fun addLinearFilteredSample(linearFilteredSample : Float) {
        _linearFilteredSamples.add(linearFilteredSample)
    }

    fun addFusionFilteredSample(fusionFilteredSample : Float) {
        _fusionFilteredSamples.add(fusionFilteredSample)
    }
}