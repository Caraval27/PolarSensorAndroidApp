package com.example.weatherapp.model

import android.util.Log
import com.example.weatherapp.data.CoordinatesData
import com.example.weatherapp.data.CoordinatesRepository
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherServerRepository

class Weather (
    private val _location: Location?,
    private val _approvedTime: String?,
    private val _weather7Days: List<WeatherDay>?,
    private val _weather24Hours: List<WeatherTime>?
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
            //updateWeather(weatherData)
            val updatedWeather = Weather(
                _location = location,
                _approvedTime = weatherData.approvedTime,
                _weather7Days = emptyList(),
                _weather24Hours = emptyList()
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

    private fun updateWeather(weatherData: WeatherData?) {
        // omvandla weatherdata till listorna med siffror i
        TODO()
        return;
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