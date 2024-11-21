package com.example.weatherapp.data

data class WeatherData (
    val approvedTime: String,
    val timeData: List<WeatherTimeData>
)

data class WeatherTimeData (
    private val validTime: String,
    private val temperature: Float,
    private val symbol: Int
)