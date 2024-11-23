package com.example.weatherapp.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.weatherapp.data.CoordinatesApiRepository
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherDbRepository
import com.example.weatherapp.data.WeatherApiRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
        if (storedWeather != null &&
            Duration.between(storedWeather._approvedTime,
                LocalDateTime.now()).toHours() < 1) {
            Log.d("Weather", Duration.between(storedWeather._approvedTime
                , LocalDateTime.now()).toHours().toString())
            return storedWeather
        }
        val coordinatesString = fetchCoordinates(location)
        if (coordinatesString == null) {
            val weatherCopy = copyWeather(ErrorType.NoCoordinates)
            return weatherCopy
        }
        Log.d("Coordinates", "Coordinate string getWeather: $coordinatesString")
        val weatherData = fetchWeather(coordinatesString)
        if (weatherData == null) {
            Log.d("Weather", "Weather data is null in getWeather.")
            val weatherCopy = copyWeather(ErrorType.NoWeather)
            return weatherCopy
        }
        val updatedWeather = Weather(
            _location = location,
            _approvedTime = LocalDateTime.parse(weatherData.approvedTime, DateTimeFormatter.ISO_DATE_TIME),
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

    private suspend fun fetchCoordinates(location: Location) : String? {
        val coordinatesData = coordinatesApiRepository.fetchCoordinates(location)
        return if (coordinatesData != null) {
            "lon/" + coordinatesData.lon + "/lat/" + coordinatesData.lat
        } else {
            null
        }
    }

    private suspend fun fetchWeather(lonLat: String) : WeatherData? {
        val weatherData = weatherApiRepository.fetchWeather(lonLat)
        if (weatherData == null) {
            Log.d("Weather", "Failed to fetch weather data in fetchWeather.")
        } else {
            Log.d("Weather", "Approved Time in fetchWeather: ${weatherData.approvedTime}")
        }
        return weatherData
    }

    private fun updateWeatherTime(weatherData: WeatherData) : List<WeatherTime> {
        if (weatherData.timeData.isEmpty()) {
            return emptyList()
        }

        val startDateTime = LocalDateTime.parse(weatherData.timeData.first().validTime, DateTimeFormatter.ISO_DATE_TIME)
        val endDateTime = startDateTime.plusHours(24)

        return weatherData.timeData.filter { weatherTimeData ->
            val validDateTime = LocalDateTime.parse(weatherTimeData.validTime, DateTimeFormatter.ISO_DATE_TIME)
            (validDateTime.isEqual(startDateTime) || validDateTime.isAfter(startDateTime)) &&
            validDateTime.isBefore(endDateTime)
        }.map { weatherTimeData ->
            WeatherTime(
                time = LocalTime.parse(weatherTimeData.validTime.substring(11, 19)), //som kommentaren nedan
                temperature = weatherTimeData.temperature.roundToInt(),
                icon = weatherTimeData.symbol
            )
        }
    }

    private fun updateWeatherDay(weatherData: WeatherData) : List<WeatherDay> {

        val groupedByDate = weatherData.timeData.groupBy { timeData ->
            LocalDate.parse(timeData.validTime.substring(0, 10)) //tror kanske det är snyggare att först göra om till dateTime och sen till date, istället för att använda substring
        }

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
    var locality: String = "Sigfridstorp",
    var county: String = "Dalarnas län",
    var municipality: String = "Vansbro"
    /*val locality: String = "Flemingsberg",
    val county: String = "Stockholms län",
    val municipality: String = "Huddinge"*/
)