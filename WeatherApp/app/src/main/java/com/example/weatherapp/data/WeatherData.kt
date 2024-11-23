package com.example.weatherapp.data

import java.time.LocalDateTime
import java.time.ZonedDateTime

data class WeatherData (
    val approvedTime: LocalDateTime,
    val timeData: List<WeatherTimeData>
)

data class WeatherTimeData (
    val validTime: LocalDateTime,
    val temperature: Float,
    val symbol: Int
)