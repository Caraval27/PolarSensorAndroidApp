package com.example.weatherapp.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.weatherapp.model.Location
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherDbRepository(
    private val applicationContext: Context
) {
    private val db = Room.databaseBuilder(
        applicationContext,
        WeatherDb::class.java, "weather_db"
    ).build()
    private val dao = db.weatherDao()

    private suspend fun toWeatherTimeEntities(weatherTimeData: List<WeatherTimeData>, location: Location) : List<WeatherTimeEntity> {
        return weatherTimeData.map { data -> WeatherTimeEntity(
            validTime = data.validTime.format(DateTimeFormatter.ISO_DATE_TIME),
            temperature = data.temperature,
            symbol = data.symbol,
            locality = location.locality,
            municipality = location.municipality,
            county = location.county
        ) }
    }

    private suspend fun toWeatherEntity(weatherData : WeatherData, location: Location) : WeatherEntity {
        val weatherEntity = WeatherEntity(
            locality = location.locality,
            municipality = location.municipality,
            county = location.county,
            approvedTime = weatherData.approvedTime.format(DateTimeFormatter.ISO_DATE_TIME)
        )
        weatherEntity.weatherTimeEntities = toWeatherTimeEntities(weatherData.weatherTimeData, location)
        return weatherEntity
    }

    private suspend fun toWeatherTimeData(weatherTimeEntities : List<WeatherTimeEntity>) : List<WeatherTimeData> {
        return weatherTimeEntities.map { entity -> WeatherTimeData(
            validTime = LocalDateTime.parse(entity.validTime),
            temperature = entity.temperature,
            symbol = entity.symbol,
        ) }
    }

    private suspend fun toWeatherData(weatherEntity : WeatherEntity) : WeatherData {
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

    /*private suspend fun toWeatherTimeEntities(weatherTimes: List<WeatherTime>, weatherId: Int) : List<WeatherTimeEntity> {
        return weatherTimes.map { weatherTime -> WeatherTimeEntity(
            time = weatherTime.time,
            temperature = weatherTime.temperature,
            icon = weatherTime.icon,
            weatherId = weatherId
        ) }
    }

    private suspend fun toWeatherDayEntities(weatherDays : List<WeatherDay>, weatherId: Int) : List<WeatherDayEntity> {
        return weatherDays.map { weatherDay -> WeatherDayEntity(
            date = weatherDay.date,
            minTemperature = weatherDay.minTemperature,
            maxTemperature = weatherDay.maxTemperature,
            mostCommonIcon = weatherDay.mostCommonIcon,
            weatherId = weatherId
        ) }
    }*/

    /*private suspend fun toWeather(weatherEntity : WeatherEntityDayAndTime) : Weather {
        val weather7Days = toWeatherDays(weatherEntity.weather7Days)
        val weather24Hours = toWeatherTimes(weatherEntity.weather24Hours)
        return Weather(
            _location = Location(
                locality = weatherEntity.weather.locality,
                municipality = weatherEntity.weather.municipality,
                county = weatherEntity.weather.county,
            ),
            _approvedTime = weatherEntity.weather.approvedTime,
            _weather7Days = weather7Days,
            _weather24Hours = weather24Hours,
            _applicationContext = applicationContext
        )
    }*/

    /*
    suspend fun getWeather(location: Location) : Weather {
        val weatherEntity = dao.getWeatherByLocation(location.locality, location.municipality, location.county)
        return toWeather(weatherEntity)
    }

    suspend fun insertWeather(weather: Weather) {
        val id = dao.insertWeather(toWeatherEntity(weather))
        dao.insertWeatherDays(toWeatherDayEntities(weather.weather7Days, id))
        dao.insertWeatherTimes(toWeatherTimeEntities(weather.weather24Hours, id))
    }

    suspend fun updateWeather(weather: Weather) {
        val locality = weather.location.locality
        val municipality = weather.location.municipality
        val county = weather.location.county
        dao.updateWeatherByLocation(locality, municipality, county, weather.approvedTime)
        weather.weather7Days.map { weatherDay -> dao.updateWeatherDayByLocation(
            weatherDay.date.format(DateTimeFormatter.ISO_LOCAL_DATE), locality, municipality,
            county, weatherDay.minTemperature, weatherDay.maxTemperature, weatherDay.mostCommonIcon) }
        weather.weather24Hours.map { weatherTime -> dao.updateWeatherTimeByLocation(
            weatherTime.time.format(DateTimeFormatter.ISO_LOCAL_TIME), locality, municipality,
            county, weatherTime.temperature, weatherTime.icon) }
    }*/
}