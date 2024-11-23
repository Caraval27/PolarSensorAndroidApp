package com.example.weatherapp.model

import java.time.LocalDate
import java.time.LocalTime

class WeatherDay (
    val date: LocalDate,
    val minTemperature: Int,
    val maxTemperature: Int,
    val mostCommonIcon: Int
)

class WeatherTime (
    val time: LocalTime,
    val temperature: Int,
    val icon: Int
)