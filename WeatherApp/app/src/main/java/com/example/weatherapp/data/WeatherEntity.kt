package com.example.weatherapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weatherapp.model.Location
import com.example.weatherapp.model.WeatherDay
import com.example.weatherapp.model.WeatherTime

@Entity(tableName = "weather")
data class WeatherEntity (
    @PrimaryKey
    val location: Location,

    val approvedTime: String,

    val weather7Days: List<WeatherDay>,

    val _weather24Hours: List<WeatherTime> = emptyList()
)