package com.example.bluetoothapp.domain

import java.time.LocalDateTime

data class Measurement (
    private var _id : Int = -1,
    private var _timeMeasured : LocalDateTime = LocalDateTime.MIN,
    val singleFilteredSamples: List<Sample> = emptyList(),
    val fusionFilteredSamples : List<Sample> = emptyList(),
    val sensorType: SensorType = SensorType.Internal
){
    val id: Int
        get() = _id

    val timeMeasured: LocalDateTime
        get() = _timeMeasured
}