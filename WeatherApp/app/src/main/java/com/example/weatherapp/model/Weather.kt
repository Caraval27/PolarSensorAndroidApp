package com.example.weatherapp.model

import android.content.Context
import android.util.Log
import com.example.weatherapp.data.CoordinatesRepository
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherDbRepository
import com.example.weatherapp.data.WeatherServerRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Weather (
    private val _location: Location = Location("", "", ""),
    private val _approvedTime: String = "",
    private val _weather7Days: List<WeatherDay> = emptyList(), // summary of weather times for 7 days
    private val _weather24Hours: List<WeatherTime> = emptyList(), // weather times today
    private val _applicationContext: Context
) {
    val location: Location
        get() = _location

    val approvedTime: String
        get() = _approvedTime

    val weather7Days: List<WeatherDay>
        get() = _weather7Days

    val weather24Hours: List<WeatherTime>
        get() = _weather24Hours

    private val weatherServerRepository = WeatherServerRepository()
    private val coordinatesRepository = CoordinatesRepository()
    private val weatherDbRepository = WeatherDbRepository(_applicationContext)

    suspend fun getWeather(location: Location) : Weather {
        // kolla ifall platsen är samma som tidiagre -->
        // var approved time för länge sen (över 1 h) --> ja: hämta ny data
        // om det är nyligen så kolla i databasen, o hämta därifrån
        // går ej längre då dem inte kan ges värden utan ett nytt object måste skapas: _location = location
            // istället skickar vi in location direct
        val coordinatesString = fetchCoordinates(location)
        Log.d("Coordinates", "Coordinate string getWeather: $coordinatesString")
        val weatherData = fetchWeather(coordinatesString)
        if (weatherData != null) {
            Log.d("Weather", "Approved Time getWeather: $_approvedTime")
            val updatedWeather = Weather(
                _location = location,
                _approvedTime = weatherData.approvedTime,
                _weather7Days = updateWeatherDay(weatherData),
                _weather24Hours = updateWeatherTime(weatherData),
                _applicationContext = _applicationContext
            )
            //saveWeather()
            return updatedWeather
        } else {
            Log.d("Weather", "Weather data is null in getWeather.")
        }

        return this
    }

    private suspend fun fetchCoordinates(location: Location) : String {
        val _coordinatesData = coordinatesRepository.fetchCoordinates(location)
        return if (_coordinatesData != null) {
            "lon/" + _coordinatesData.lon + "/lat/" + _coordinatesData.lat
        } else {
            ""
        }
    }

    private suspend fun fetchWeather(lonLat: String) : WeatherData? {
        val _weatherData = weatherServerRepository.fetchWeather(lonLat)
        if (_weatherData != null) {
            Log.d("Weather", "Approved Time in fetchWeather: ${_weatherData.approvedTime}")
        } else {
            Log.d("Weather", "Failed to fetch weather data in fetchWeather.")
        }
        return _weatherData
    }

    // ej testad
    private fun updateWeatherTime(weatherData: WeatherData?) : List<WeatherTime> {
        if (weatherData?.timeData.isNullOrEmpty()) return emptyList()

        val startDateTime = LocalDateTime.parse(weatherData?.timeData?.first()?.validTime, DateTimeFormatter.ISO_DATE_TIME)
        val endDateTime = startDateTime.plusHours(24)

        return weatherData?.timeData?.filter { weatherTimeData ->
            val validDateTime = LocalDateTime.parse(weatherTimeData.validTime, DateTimeFormatter.ISO_DATE_TIME)
            validDateTime.isAfter(startDateTime) && validDateTime.isBefore(endDateTime)
        }?.map { weatherTimeData ->
            WeatherTime(
                time = LocalTime.parse(weatherTimeData.validTime.substring(11, 19)),
                temperature = weatherTimeData.temperature.toInt(),
                icon = weatherTimeData.symbol
            )
        } ?: emptyList()
    }

    // ej testad
    private fun updateWeatherDay(weatherData: WeatherData?) : List<WeatherDay> {
        if (weatherData?.timeData.isNullOrEmpty()) return emptyList()

        val groupedByDate = weatherData?.timeData?.groupBy { timeData ->
            LocalDate.parse(timeData.validTime.substring(0, 10))
        }

        return groupedByDate?.map { (date, weatherTimes) ->
            val minTemperature = weatherTimes.minOfOrNull { it.temperature.toInt() } ?: 0
            val maxTemperature = weatherTimes.maxOfOrNull { it.temperature.toInt() } ?: 0
            val mostCommonIcon = weatherTimes.groupingBy { it.symbol }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: 0

            Log.d("Weather", "In update day: $date")

            WeatherDay(
                date = date,
                minTemperature = minTemperature,
                maxTemperature = maxTemperature,
                mostCommonIcon = mostCommonIcon
            )
        } ?: emptyList()
    }

    private fun saveWeather() {
        TODO()
        return;
    }
}

data class Location (
    val locality: String = "Flemingsberg",
    val county: String = "Stockholm",
    val municipality: String = "Huddinge kommun"
)