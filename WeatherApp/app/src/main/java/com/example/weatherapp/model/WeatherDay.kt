package com.example.weatherapp.model

import java.time.LocalDate

class WeatherDay (
    val date: LocalDate,
    val minTemperature: Int,
    val maxTemperature: Int,
    val mostCommonIcon: Int
)