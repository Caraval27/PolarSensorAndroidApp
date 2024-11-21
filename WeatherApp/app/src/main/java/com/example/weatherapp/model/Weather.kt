package com.example.weatherapp.model

import android.util.Log
import com.example.weatherapp.data.CoordinatesRepository
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherServerRepository
import java.time.LocalDate
import java.time.LocalTime

class Weather (
    private val _location: Location?,
    private val _approvedTime: String?,
    private val _weather7Days: List<WeatherDay>?, // summary of weather times for 7 days
    private val _weather24Hours: List<WeatherTime>? // weather times today
) {
    val location: Location?
        get() = _location

    val approvedTime: String?
        get() = _approvedTime

    val weather7Days: List<WeatherDay>?
        get() = _weather7Days

    val weather24Hours: List<WeatherTime>?
        get() = _weather24Hours

    private val weatherServerRepository = WeatherServerRepository()
    private val coordinatesRepository = CoordinatesRepository()

    suspend fun getWeather(location: Location) : Weather {
        // kolla ifall platsen är samma som tidiagre -->
        // var approved time för länge sen? --> ja: hämta ny data
        // om det är nyligen så kolla i databasen, o hämta därifrån
        // går ej längre då dem inte kan ges värden utan ett nytt object måste skapas: _location = location
            // istället skickar vi in location direct
        val coordinatesString = fetchCoordinates(location)
        Log.d("Coordinates", "Coordinate string getWeather: $coordinatesString")

        //val weatherData = fetchWeather(coordinatesString)
        val weatherData = fetchWeather("lon/14.333/lat/60.38")
        if (weatherData != null) {
            Log.d("Weather", "Approved Time getWeather: $_approvedTime")

            val updatedWeather = Weather(
                _location = location,
                _approvedTime = weatherData.approvedTime,
                _weather7Days = weather7Days, // updateWeatherDay can returnera en lista av hela veckan
                _weather24Hours = updateWeatherTime(weatherData) // om det blir en lista skicka in första weatherData.get(0)
            )
            //saveWeather()
            return updatedWeather
        } else {
            Log.d("Weather", "Weather data is null in getWeather.")
        }

        return this
    }

    private suspend fun fetchCoordinates(location: Location) : String {
        //val locality = "Sigfridstorp"
        var _coordinatesData = coordinatesRepository.fetchCoordinates(location)
        if (_coordinatesData != null) {
            return "lon/" + _coordinatesData.lon + "/lat/" + _coordinatesData.lat
        } else {
            return ""
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

    private fun updateWeatherTime(weatherData: WeatherData?) : List<WeatherTime> {
        return weatherData?.timeData?.map { weatherTimeData ->
            WeatherTime(
                time = LocalTime.parse(weatherTimeData.validTime),
                temperature = weatherTimeData.temperature.toInt(),
                icon = weatherTimeData.symbol
            )
        } ?: emptyList()
    }

    private fun updateWeatherDay(weatherData: WeatherData) : WeatherDay? {
        val date = weatherData.approvedTime?.let { LocalDate.parse(it.substring(0, 10)) }

        if (date == null || weatherData.timeData.isNullOrEmpty()) {
            return null
        }

        val weatherTimes = updateWeatherTime(weatherData)

        val minTemperature = weatherTimes.minOfOrNull { it.temperature } ?: 0
        val maxTemperature = weatherTimes.maxOfOrNull { it.temperature } ?: 0
        val mostCommonIcon = weatherTimes.groupingBy { it.icon }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: 0

        return WeatherDay(
            date = date,
            minTemperature = minTemperature,
            maxTemperature = maxTemperature,
            mostCommonIcon = mostCommonIcon
        )
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