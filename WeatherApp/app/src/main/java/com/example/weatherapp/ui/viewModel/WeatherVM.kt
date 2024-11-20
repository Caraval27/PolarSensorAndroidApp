package com.example.weatherapp.ui.viewModel

import androidx.lifecycle.ViewModel
import com.example.weatherapp.model.Weather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WeatherVM : ViewModel() {
    private val _weather = MutableStateFlow(Weather())
    val weather: StateFlow<Weather>
        get() = _weather.asStateFlow()

    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState>
        get() = _weatherState.asStateFlow()


    init {
        _weather.value.getWeather(_weatherState.value.selectedLocation)
    }

}


enum class ViewType {
    Week,
    Day
}

data class WeatherState (
    val viewType: ViewType = ViewType.Day,
    val selectedLocation: String = "Flemingsberg"
)