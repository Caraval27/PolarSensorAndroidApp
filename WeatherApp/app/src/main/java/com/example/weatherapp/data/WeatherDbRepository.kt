package com.example.weatherapp.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.weatherapp.model.Location
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherDbRepository(
    applicationContext: Context
) {
    private val db = Room.databaseBuilder(
        applicationContext,
        WeatherDb::class.java, "weather_db"
    ).build()
    private val dao = db.weatherDao()

    private fun toWeatherTimeEntities(weatherTimeData: List<WeatherTimeData>, location: Location) : List<WeatherTimeEntity> {
        return weatherTimeData.map { data -> WeatherTimeEntity(
            validTime = data.validTime.format(DateTimeFormatter.ISO_DATE_TIME),
            temperature = data.temperature,
            symbol = data.symbol,
            locality = location.locality,
            municipality = location.municipality,
            county = location.county
        ) }
    }

    private fun toWeatherEntity(weatherData : WeatherData, location: Location) : WeatherEntity {
        val weatherEntity = WeatherEntity(
            locality = location.locality,
            municipality = location.municipality,
            county = location.county,
            approvedTime = weatherData.approvedTime.format(DateTimeFormatter.ISO_DATE_TIME)
        )
        weatherEntity.weatherTimeEntities = toWeatherTimeEntities(weatherData.weatherTimeData, location)
        return weatherEntity
    }

    private fun toWeatherTimeData(weatherTimeEntities : List<WeatherTimeEntity>) : List<WeatherTimeData> {
        return weatherTimeEntities.map { entity -> WeatherTimeData(
            validTime = LocalDateTime.parse(entity.validTime),
            temperature = entity.temperature,
            symbol = entity.symbol,
        ) }
    }

    private fun toWeatherData(weatherEntity : WeatherEntity) : WeatherData {
        val weatherTimeData = toWeatherTimeData(weatherEntity.weatherTimeEntities)
        return WeatherData(
            approvedTime = LocalDateTime.parse(weatherEntity.approvedTime),
            weatherTimeData = weatherTimeData
        )
    }

    suspend fun insertWeather(weatherData: WeatherData, location: Location) {
        Log.d("WeatherDbRepository", "Insert approved time:" + weatherData.approvedTime)
        val weatherEntity = toWeatherEntity(weatherData, location);
        dao.insertWeatherWithTime(weatherEntity)
    }

    suspend fun getWeather(location: Location) : WeatherData? {
        Log.d("WeatherDbRepository", "Entering get")
        val weatherEntity = dao.getWeatherWithTimeByLocation(location) ?: return null
        Log.d("WeatherDbRepository", "Get approved time:" + weatherEntity.approvedTime)
        return toWeatherData(weatherEntity)
    }
}