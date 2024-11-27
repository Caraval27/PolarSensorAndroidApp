package com.example.bluetoothapp.data

import java.time.LocalDateTime

data class MeasurementData(
    val id : Int,
    val timeMeasured : LocalDateTime,
    val sampleData: List<SampleData>
)

data class SampleData (
    val singleFilterValue : Float,
    val fusionFilterValue : Float
)