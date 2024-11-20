package com.example.weatherapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weatherapp.model.WeatherDay
import com.example.weatherapp.model.WeatherTime
import java.time.LocalTime

@Entity(tableName = "weather")
data class WeatherEntity (
    @PrimaryKey
    val locality: String,

    val municipality: String,

    val county: String,

    val approvedTime: String,

    val weather7Days: List<WeatherDay>,

    val _weather24Hours: List<WeatherTime> = emptyList()
)

data class WeatherDayEntity (
    val time: LocalTime,
    val temperature: Int,
    val icon: Int
)

class WeatherTime (
    val time: LocalTime,
    val temperature: Int,
    val icon: Int
)