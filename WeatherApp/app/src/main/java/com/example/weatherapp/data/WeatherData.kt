package com.example.weatherapp.data

import java.time.LocalDateTime

class WeatherData (
    private val validTime: String,
    private val temperature: Float,
    private val symbol: Int
) {
    fun getValue() : String {
        return validTime
    }
}