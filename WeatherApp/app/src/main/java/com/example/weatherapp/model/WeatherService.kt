package com.example.weatherapp.model

import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherRepository

class WeatherService {
    private val weatherRepository = WeatherRepository()
    //private val coordinatesRepository = CoordinatesRepository()

    private var _weatherData: List<WeatherData> = listOf()
    val weatherData: List<WeatherData> = _weatherData

    fun fetchWeather(lonLat: String) : String { // ska vara privat sen
        val ll = "lonLat=lon/14.333/lat/60.38"
        weatherRepository.fetchWeather(ll) { processedData ->
            _weatherData = processedData }
        return _weatherData.get(0).getValue() // ska bort sen bara för test
    }

    fun getWeather() : Weather {
        TODO()
    }

// metod som fixar time och day - beräkningar
}