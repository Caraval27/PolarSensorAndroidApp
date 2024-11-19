package com.example.weatherapp.model

import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.data.WeatherRepository

class WeatherService {
    private val weatherRepository = WeatherRepository()
    //private val coordinatesRepository = CoordinatesRepository()

    private var _weatherData: List<WeatherData> = listOf()
    val weatherData: List<WeatherData> = _weatherData

    private fun fetchWeather(lonLat: String) {
        weatherRepository.fetchWeather(lonLat) { processedData ->
            _weatherData = processedData }
    }


    // metod som fixar time och day
}