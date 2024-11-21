package com.example.weatherapp.model

import android.content.Context
import android.util.Log
import com.example.weatherapp.data.CoordinatesData
import com.example.weatherapp.data.CoordinatesRepository
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherDbRepository
import com.example.weatherapp.data.WeatherServerRepository

class Weather (
    private val _location: Location = Location("", "", ""),
    private val _approvedTime: String = "",
    private val _weather7Days: List<WeatherDay> = emptyList(),
    private val _weather24Hours: List<WeatherTime> = emptyList(),
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
        //val coordinatesString = fetchCoordinates(location)
        //val weatherData = fetchWeather(coordinatesString)
        val weatherData = fetchWeather("lon/14.333/lat/60.38")
        if (weatherData != null) {
            Log.d("Weather", "Approved Time getWeather: $_approvedTime")
            //updateWeather(weatherData)
            val updatedWeather = Weather(
                _location = location,
                _approvedTime = weatherData.approvedTime,
                _weather7Days = emptyList(),
                _weather24Hours = emptyList(),
                _applicationContext = _applicationContext
            )
            //saveWeather()
            return updatedWeather
        } else {
            Log.d("Weather", "Weather data is null in getWeather.")
        }

        return this
    }

    private fun fetchCoordinates() : String {
        //val locality = "Sigfridstorp"
        var _coordinatesData: CoordinatesData? = null
        coordinatesRepository.fetchCoordinates(_location) { coordinatesData ->
            _coordinatesData = coordinatesData }
        if (_coordinatesData == null) {
            return ""
        } else {
            return "lon/" + _coordinatesData!!.lon + "/lat/" + _coordinatesData!!.lat
        }
    }

    private suspend fun fetchWeather(lonLat: String) : WeatherData? {
        val ll = "lon/14.333/lat/60.38" // temp
        val _weatherData = weatherServerRepository.fetchWeather(ll)
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