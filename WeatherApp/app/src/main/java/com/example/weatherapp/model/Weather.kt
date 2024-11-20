package com.example.weatherapp.model

import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherRepository

class Weather {
    private var _place: String? = null
    val place: String?
        get() = _place

    private var _approvedTime: String? = null
    val approvedTime: String?
        get() = _approvedTime

    private var _weather7Days: List<WeatherDay> = emptyList()
    val weather7Days: List<WeatherDay>
        get() = _weather7Days

    private var _weather24Hours: List<WeatherTime> = emptyList()
    val weather24Hours: List<WeatherTime>
        get() = _weather24Hours

    private val weatherRepository = WeatherRepository()
    //private val coordinatesRepository = CoordinatesRepository()

    fun getWeather(place: String) {
        // kolla ifall platsen är samma som tidiagre -->
        // var approved time för länge sen? --> ja: hämta ny data
        // om det är nyligen så kolla i databasen, o hämta därifrån
        _place = place
        val coordinatesString = fetchCoordinates()
        val weatherData = fetchWeather(coordinatesString)

        updateWeather(weatherData)
        saveWeather()
        TODO()
    }

    private fun fetchCoordinates() : String {
        //använd _place
        TODO()
    }

    private fun fetchWeather(lonLat: String) : WeatherData? {
        val ll = "lonLat=lon/14.333/lat/60.38" // temp
        var _weatherData: WeatherData? = null
        weatherRepository.fetchWeather(ll) { weatherData ->
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