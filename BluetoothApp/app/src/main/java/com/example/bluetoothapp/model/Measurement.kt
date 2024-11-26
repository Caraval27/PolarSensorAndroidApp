package com.example.bluetoothapp.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Measurement (
    private var id : Int,
    private var _timeMeasured : LocalDateTime,
    private var _samples : List<Sample>
){
    val timeMeasured: LocalDateTime
        get() = _timeMeasured

    val samples: List<Sample>
        get() = _samples
}