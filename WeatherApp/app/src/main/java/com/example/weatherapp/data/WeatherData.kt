package com.example.weatherapp.data

data class WeatherData (
    val approvedTime: String,
    val timeData: List<WeatherTimeData>
)

data class WeatherTimeData (
    val validTime: String,
    val temperature: Float,
    val symbol: Int
)