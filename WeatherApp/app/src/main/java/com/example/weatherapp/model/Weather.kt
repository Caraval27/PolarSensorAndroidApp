package com.example.weatherapp.model

import com.example.weatherapp.data.CoordinatesData
import com.example.weatherapp.data.CoordinatesRepository
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherServerRepository

class Weather {
    private var _location: Location? = null
    val location: Location?
        get() = _location

    private var _approvedTime: String? = null
    val approvedTime: String?
        get() = _approvedTime

    private var _weather7Days: List<WeatherDay> = emptyList()
    val weather7Days: List<WeatherDay>
        get() = _weather7Days

    private var _weather24Hours: List<WeatherTime> = emptyList()
    val weather24Hours: List<WeatherTime>
        get() = _weather24Hours

    private val weatherServerRepository = WeatherServerRepository()
    private val coordinatesRepository = CoordinatesRepository()

    fun getWeather(location: Location) {
        // kolla ifall platsen är samma som tidiagre -->
        // var approved time för länge sen? --> ja: hämta ny data
        // om det är nyligen så kolla i databasen, o hämta därifrån
        _location = location
        val coordinatesString = fetchCoordinates()
        val weatherData = fetchWeather(coordinatesString)

        updateWeather(weatherData)
        saveWeather()
        TODO()
    }

    private fun fetchCoordinates() : String {
        val locality = _location?.locality ?: ""
        val displayName = _location?.locality + ", " + _location?.municipality + ", " + location?.county
        //val locality = "Sigfridstorp"
        var _coordinatesData: CoordinatesData? = null
        coordinatesRepository.fetchCoordinates(locality, displayName) { coordinatesData ->
            _coordinatesData = coordinatesData }
        if (_coordinatesData == null) {
            return ""
        } else {
            return "lon/" + _coordinatesData!!.lon + "/lat/" + _coordinatesData!!.lat
        }
    }

    private fun fetchWeather(lonLat: String) : WeatherData? {
        val ll = "lon/14.333/lat/60.38" // temp
        var _weatherData: WeatherData? = null
        weatherServerRepository.fetchWeather(ll) { weatherData ->
            _weatherData = weatherData }
        return _weatherData
    }

    private fun updateWeather(weatherData: WeatherData?) {
        // omvandla weatherdata till listorna med siffror i
        TODO()
    }

    private fun saveWeather() {
        TODO()
    }
}

data class Location (
    val locality: String = "Flemingsberg",
    val county: String = "Stockholm",
    val municipality: String = "Huddinge kommun"
)