package com.example.weatherapp.ui.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.Location
import com.example.weatherapp.model.Weather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherVM(
    application: Application
) : AndroidViewModel(application) {
    private val _weather = MutableStateFlow(Weather(Location("", "", ""),
        "", emptyList(), emptyList(), application.applicationContext))
    val weather: StateFlow<Weather>
        get() = _weather.asStateFlow()

    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState>
        get() = _weatherState.asStateFlow()

    init {
        viewModelScope.launch {
            _weather.value = _weather.value.getWeather(_weatherState.value.selectedLocation)
            Log.d("WeatherVM", "Updated Weather: Approved Time = ${_weather.value.approvedTime}")
        }
    }
}

enum class ViewType {
    Week,
    Day
}

data class WeatherState (
    val viewType: ViewType = ViewType.Day,
    val selectedLocation: Location = Location("Flemingsberg", "Stockholm", "Huddinge kommun")
)