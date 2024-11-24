package com.example.weatherapp.data

import java.time.LocalDateTime

data class WeatherData (
    val approvedTime: LocalDateTime,
    val weatherTimeData: List<WeatherTimeData>
)

data class WeatherTimeData (
    val validTime: LocalDateTime,
    val temperature: Int,
    val symbol: Int
)