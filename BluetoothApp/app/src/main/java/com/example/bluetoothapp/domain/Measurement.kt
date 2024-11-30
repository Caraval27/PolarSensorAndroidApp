package com.example.bluetoothapp.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

data class Measurement (
    private var _id : Int = -1,
    private var _timeMeasured : LocalDateTime = LocalDateTime.MIN,
    val linearFilteredSamples: List<Float> = emptyList(),
    val fusionFilteredSamples : List<Float> = emptyList(),
){
    val id: Int
        get() = _id

    val timeMeasured: LocalDateTime
        get() = _timeMeasured
}