package com.example.bluetoothapp.domain

data class SensorData (
    val xValue: Float = Float.NaN,
    val yValue: Float = Float.NaN,
    val zValue: Float = Float.NaN,
    val timeStamp: Long = -1,
)