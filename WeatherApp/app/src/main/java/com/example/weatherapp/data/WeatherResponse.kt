package com.example.weatherapp.data

data class WeatherResponse(
    val referenceTime: String,
    val approvedTime: String,
    val timeSeries: List<TimeSeries>
)

data class TimeSeries(
    val validTime: String,
    val parameters: List<Parameter>
)

data class Parameter(
    val name: String,
    val value: List<Float>,
    val unit: String
)