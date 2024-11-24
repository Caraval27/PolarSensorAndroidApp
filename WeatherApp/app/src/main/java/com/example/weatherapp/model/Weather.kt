package com.example.weatherapp.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.weatherapp.data.CoordinatesApiRepository
import com.example.weatherapp.data.CoordinatesData
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherDbRepository
import com.example.weatherapp.data.WeatherApiRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

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

    val approvedTime: LocalDateTime
        get() = _approvedTime

    val weather7Days: List<WeatherDay>
        get() = _weather7Days

    val weather24Hours: List<WeatherTime>
        get() = _weather24Hours

    val errorType: ErrorType
        get() = _errorType

    private val weatherApiRepository = WeatherApiRepository()
    private val coordinatesApiRepository = CoordinatesApiRepository()
    private val weatherDbRepository = WeatherDbRepository(_applicationContext)

    suspend fun updateWeather(location: Location) : Weather {
        val storedWeather = weatherDbRepository.getWeather(location)
        val connectivityManager = _applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (networkCapabilities == null || !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            Log.d("Weather", "No internet connection")
            if (storedWeather == null) {
                val weatherCopy = copyWeather(ErrorType.NoConnection)
                return weatherCopy
            }
            storedWeather._errorType = ErrorType.NoConnection
            return storedWeather
        }
        val currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Stockholm")).toLocalDateTime()
        if (storedWeather != null &&
            Duration.between(storedWeather._approvedTime, currentDateTime).toHours() < 1) {
            Log.d("Weather", Duration.between(storedWeather._approvedTime, currentDateTime).toHours().toString())
            return storedWeather
        }

        val coordinatesData = coordinatesApiRepository.fetchCoordinates(location)
        if (coordinatesData == null) {
            val weatherCopy = copyWeather(ErrorType.NoCoordinates)
            return weatherCopy
        }
        //val coordinatesData = CoordinatesData(14.333, 60.38)
        Log.d("Coordinates", "Coordinate string getWeather: ${coordinatesData.lon} and ${coordinatesData.lat}")
        val weatherData = weatherApiRepository.fetchWeather(coordinatesData)
        if (weatherData == null) {
            Log.d("Weather", "Weather data is null in getWeather.")
            val weatherCopy = copyWeather(ErrorType.NoWeather)
            return weatherCopy
        }
        val updatedWeather = Weather(
            _location = location,
            _approvedTime = weatherData.approvedTime,
            _weather7Days = updateWeatherDay(weatherData),
            _weather24Hours = updateWeatherTime(weatherData),
            _applicationContext = _applicationContext
        )
        weatherDbRepository.insertWeather(updatedWeather)
        return updatedWeather
    }

    private suspend fun copyWeather(errorType: ErrorType) : Weather {
        return Weather(
            _location = _location,
            _approvedTime = _approvedTime,
            _weather7Days = _weather7Days,
            _weather24Hours = _weather24Hours,
            _errorType = errorType,
            _applicationContext = _applicationContext
        )
    }

    private fun updateWeatherTime(weatherData: WeatherData) : List<WeatherTime> {
        val currentHour = ZonedDateTime.now(ZoneId.of("Europe/Stockholm")).toLocalDateTime()
            .withMinute(0).withSecond(0)
        Log.d("Weather", "Current hour:" + currentHour)
        val startDateTime = weatherData.timeData.map { it.validTime }/*.filter { it >= currentHour }*/
            .minOrNull()
        if (startDateTime == null) {
            return emptyList()
        }
        Log.d("Weather", "Start time: " + startDateTime)
        val endDateTime = startDateTime.plusHours(24)

        return weatherData.timeData.filter { weatherTimeData ->
            weatherTimeData.validTime >= startDateTime &&
                    weatherTimeData.validTime < endDateTime
        }.map { weatherTimeData ->
            WeatherTime(
                time = weatherTimeData.validTime.toLocalTime(),
                temperature = weatherTimeData.temperature.roundToInt(),
                icon = weatherTimeData.symbol
            )
        }
    }

    private fun updateWeatherDay(weatherData: WeatherData) : List<WeatherDay> {
        val currentDate = ZonedDateTime.now(ZoneId.of("Europe/Stockholm")).toLocalDate()
        val groupedByDate = weatherData.timeData.groupBy { it.validTime.toLocalDate() }
            .filterKeys { it >= currentDate }

        return groupedByDate.map { (date, weatherTimes) ->
            val minTemperature = weatherTimes.minOf { it.temperature.roundToInt() }
            val maxTemperature = weatherTimes.maxOf { it.temperature.roundToInt() }
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
    /*var locality: String = "Sigfridstorp",
    var county: String = "Dalarnas län",
    var municipality: String = "Vansbro"*/
    val locality: String = "Flemingsberg",
    val county: String = "Stockholms län",
    val municipality: String = "Huddinge"
)