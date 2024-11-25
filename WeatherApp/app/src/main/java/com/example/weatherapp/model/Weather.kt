package com.example.weatherapp.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.saveable.mapSaver
import com.example.weatherapp.data.CoordinatesApiRepository
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherDbRepository
import com.example.weatherapp.data.WeatherApiRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class Weather (
    private var _location: Location = Location(),
    private var _approvedTime: LocalDateTime = LocalDateTime.MIN,
    private var _weather7Days: List<WeatherDay> = emptyList(),
    private var _weather24Hours: List<WeatherTime> = emptyList(),
    private var _errorType: ErrorType = ErrorType.None,
    private var _applicationContext: Context
) {
    val location: Location
        get() = _location

    val weather7Days: List<WeatherDay>
        get() = _weather7Days

    val weather24Hours: List<WeatherTime>
        get() = _weather24Hours

    val errorType: ErrorType
        get() = _errorType

    private val weatherApiRepository = WeatherApiRepository()
    private val coordinatesApiRepository = CoordinatesApiRepository()
    private val weatherDbRepository = WeatherDbRepository(_applicationContext)

    suspend fun getWeather(location: Location) : Weather {
        val weather = getOldWeather(location)

        if (weather != null) {
            return weather
        }

        val coordinatesData = coordinatesApiRepository.fetchCoordinates(location)
        if (coordinatesData == null) {
            return copyWeather(ErrorType.NoCoordinates)
        }
        val weatherData = weatherApiRepository.fetchWeather(coordinatesData)
        if (weatherData == null) {
            return copyWeather(ErrorType.NoWeather)
        }
        weatherDbRepository.insertWeather(weatherData, location)
        return getWeather(weatherData, coordinatesData.location, ErrorType.None)
    }

    private suspend fun getOldWeather(location: Location) : Weather? {
        val currentDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime()
        if (location == _location && Duration.between(_approvedTime, currentDateTime).toHours() < 1) {
            return copyWeather(ErrorType.None)
        }
        val storedWeatherData = weatherDbRepository.getWeather(location)
        val connectivityManager = _applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (networkCapabilities == null || !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            if (storedWeatherData == null) {
                return copyWeather(ErrorType.NoConnection)
            }
            return getWeather(storedWeatherData, location, ErrorType.NoConnection)
        }
        if (storedWeatherData != null &&
            Duration.between(storedWeatherData.approvedTime, currentDateTime).toHours() < 1) {
            return getWeather(storedWeatherData, location, ErrorType.None)
        }
        return null;
    }

    private fun copyWeather(errorType: ErrorType) : Weather {
        return Weather(
            _location = _location,
            _approvedTime = _approvedTime,
            _weather7Days = _weather7Days,
            _weather24Hours = _weather24Hours,
            _errorType = errorType,
            _applicationContext = _applicationContext)
    }

    private fun getWeather(weatherData: WeatherData, location: Location, errorType: ErrorType) : Weather {
        val weather7Days = updateWeatherDay(weatherData)
        val weather24Hours = updateWeatherTime(weatherData)
        var _errorType = errorType
        if (weather24Hours.isEmpty() && errorType == ErrorType.None) {
            _errorType = ErrorType.NoWeather
        }
        return Weather(
            _location = location,
            _approvedTime = weatherData.approvedTime,
            _weather7Days = weather7Days,
            _weather24Hours = weather24Hours,
            _errorType = _errorType,
            _applicationContext = _applicationContext
        )
    }

    private fun updateWeatherTime(weatherData: WeatherData) : List<WeatherTime> {
        val currentHour = LocalDateTime.now(ZoneOffset.UTC).withMinute(0).withSecond(0)
        val startDateTime = weatherData.weatherTimeData
            .map { it.validTime }
            .filter { it >= currentHour }
            .minOrNull()
        if (startDateTime == null) {
            return emptyList()
        }
        val endDateTime = startDateTime.plusHours(24)

        return weatherData.weatherTimeData.filter { weatherTimeData ->
            weatherTimeData.validTime >= startDateTime &&
                    weatherTimeData.validTime < endDateTime
        }.map { weatherTimeData ->
            WeatherTime(
                time = weatherTimeData.validTime.atZone(ZoneId.of("Europe/Stockholm")).toLocalTime(),
                temperature = weatherTimeData.temperature,
                icon = weatherTimeData.symbol
            )
        }
    }

    private fun updateWeatherDay(weatherData: WeatherData) : List<WeatherDay> {
        val currentDate = LocalDate.now(ZoneOffset.UTC)
        val groupedByDate = weatherData.weatherTimeData.groupBy { it.validTime.toLocalDate() }
            .filterKeys { it >= currentDate }

        return groupedByDate.map { (date, weatherTimes) ->
            val minTemperature = weatherTimes.minOf { it.temperature }
            val maxTemperature = weatherTimes.maxOf { it.temperature }
            val mostCommonIcon = weatherTimes.groupingBy { it.symbol }
                .eachCount()
                .maxBy { it.value }.key

            WeatherDay(
                date = date,
                minTemperature = minTemperature,
                maxTemperature = maxTemperature,
                mostCommonIcon = mostCommonIcon
            )
        }
    }
}

enum class ErrorType {
    None,
    NoConnection,
    NoCoordinates,
    NoWeather,
}

data class Location (
    val locality: String = "Flemingsberg",
    val county: String = "Stockholms län",
    val municipality: String = "Huddinge"
) {
    companion object {
        val Saver = mapSaver(
            save = { location ->
                mapOf(
                    "locality" to location.locality,
                    "county" to location.county,
                    "municipality" to location.municipality
                )
            },
            restore = { map ->
                Location(
                    locality = map["locality"] as String,
                    county = map["county"] as String,
                    municipality = map["municipality"] as String
                )
            }
        )
    }
}