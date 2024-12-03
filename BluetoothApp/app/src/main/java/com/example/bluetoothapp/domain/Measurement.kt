package com.example.bluetoothapp.domain

import java.time.LocalDateTime

data class Measurement (
    val id : Int = -1,
    val timeMeasured : LocalDateTime = LocalDateTime.MIN,
    val singleFilteredSamples: List<Sample> = emptyList(),
    val fusionFilteredSamples : List<Sample> = emptyList(),
    val sensorType: SensorType = SensorType.Internal
)