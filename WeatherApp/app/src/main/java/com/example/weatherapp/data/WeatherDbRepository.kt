package com.example.weatherapp.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.weatherapp.model.Location
import com.example.weatherapp.model.Weather
import com.example.weatherapp.model.WeatherDay
import com.example.weatherapp.model.WeatherTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class WeatherDbRepository(
    private val applicationContext: Context
) {
    private val db = Room.databaseBuilder(
        applicationContext,
        WeatherDb::class.java, "weather_db"
    ).build()
    private val dao = db.weatherDao()

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

    private suspend fun toWeatherTimeEntities(weatherTimes: List<WeatherTime>, location: Location) : List<WeatherTimeEntity> {
        return weatherTimes.map { weatherTime -> WeatherTimeEntity(
            time = weatherTime.time.format(DateTimeFormatter.ISO_LOCAL_TIME),
            temperature = weatherTime.temperature,
            icon = weatherTime.icon,
            locality = location.locality,
            municipality = location.municipality,
            county = location.county
        ) }
    }

    private suspend fun toWeatherDayEntities(weatherDays : List<WeatherDay>, location: Location) : List<WeatherDayEntity> {
        return weatherDays.map { weatherDay -> WeatherDayEntity(
            date = weatherDay.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            minTemperature = weatherDay.minTemperature,
            maxTemperature = weatherDay.maxTemperature,
            mostCommonIcon = weatherDay.mostCommonIcon,
            locality = location.locality,
            municipality = location.municipality,
            county = location.county
        ) }
    }

    private suspend fun toWeatherEntity(weather : Weather) : WeatherEntity {
        val weatherEntity = WeatherEntity(
            locality = weather.location.locality,
            municipality = weather.location.municipality,
            county = weather.location.county,
            approvedTime = weather.approvedTime.format(DateTimeFormatter.ISO_DATE_TIME)
        )
        weatherEntity.weather7Days = toWeatherDayEntities(weather.weather7Days, weather.location)
        weatherEntity.weather24Hours = toWeatherTimeEntities(weather.weather24Hours, weather.location)
        return weatherEntity
    }

    private suspend fun toWeatherTimes(weatherTimeEntities : List<WeatherTimeEntity>) : List<WeatherTime> {
        return weatherTimeEntities.map { weatherTimeEntity -> WeatherTime(
            time = LocalTime.parse(weatherTimeEntity.time, DateTimeFormatter.ISO_LOCAL_TIME),
            temperature = weatherTimeEntity.temperature,
            icon = weatherTimeEntity.icon,
        ) }
    }

    private suspend fun toWeatherDays(weatherDayEntities : List<WeatherDayEntity>) : List<WeatherDay> {
        return weatherDayEntities.map { weatherDayEntity -> WeatherDay(
            date = LocalDate.parse(weatherDayEntity.date, DateTimeFormatter.ISO_LOCAL_DATE),
            minTemperature = weatherDayEntity.minTemperature,
            maxTemperature = weatherDayEntity.maxTemperature,
            mostCommonIcon = weatherDayEntity.mostCommonIcon,
        ) }
    }

    private suspend fun toWeather(weatherEntity : WeatherEntity) : Weather {
        val weather7Days = toWeatherDays(weatherEntity.weather7Days)
        val weather24Hours = toWeatherTimes(weatherEntity.weather24Hours)
        return Weather(
            _location = Location(
                locality = weatherEntity.locality,
                municipality = weatherEntity.municipality,
                county = weatherEntity.county,
            ),
            _approvedTime = LocalDateTime.parse(weatherEntity.approvedTime, DateTimeFormatter.ISO_DATE_TIME),
            _weather7Days = weather7Days,
            _weather24Hours = weather24Hours,
            _applicationContext = applicationContext
        )
    }

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

    suspend fun insertWeather(weather: Weather) {
        Log.d("WeatherDbRepository", "Insert approved time:" + weather.approvedTime)
        val weatherEntity = toWeatherEntity(weather);
        dao.insertWeatherDayAndTime(weatherEntity)
        //fånga exceptions?
    }

    suspend fun getWeather(location: Location) : Weather? {
        Log.d("WeatherDbRepository", "Entering get")
        val weatherEntity = dao.getWeatherDayAndTimeByLocation(location) ?: return null
        Log.d("WeatherDbRepository", "Get approved time:" + weatherEntity.approvedTime)
        return toWeather(weatherEntity)
    }

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