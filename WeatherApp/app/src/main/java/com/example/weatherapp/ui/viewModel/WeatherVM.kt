package com.example.weatherapp.ui.viewModel

import androidx.lifecycle.ViewModel
import com.example.weatherapp.data.WeatherData
import com.example.weatherapp.model.Weather
import com.example.weatherapp.model.WeatherService

class WeatherVM : ViewModel() {
    private val weatherService: WeatherService
    private val weather: Weather
    private val weatherData: WeatherData

    init {
        weatherService = WeatherService()
        weather = weatherService.getWeather()
        weatherData = weatherService.fetchWeather("123");
    }

    fun weatherData() : WeatherData {
        return weatherData
    }
}

data class WeatherState (
    val idk: Int = 1
)