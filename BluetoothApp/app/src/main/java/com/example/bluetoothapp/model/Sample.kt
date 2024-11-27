package com.example.bluetoothapp.model

import java.time.LocalDateTime

data class Sample(
    val x: Float,
    val y: Float,
    val z: Float,
    val time: LocalDateTime
)

// vet ej vad vi hämtar från frontend tror denna ska innehålla xyz värden, dvs rå data