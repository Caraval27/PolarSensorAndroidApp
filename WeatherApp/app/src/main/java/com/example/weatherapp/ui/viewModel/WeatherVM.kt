package com.example.weatherapp.ui.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
    private val _weather = MutableStateFlow(Weather( _applicationContext =  application.applicationContext))
    val weather: StateFlow<Weather>
        get() = _weather.asStateFlow()

    private val _weatherState = MutableStateFlow(WeatherState())
    val weatherState: StateFlow<WeatherState>
        get() = _weatherState.asStateFlow()

    fun updateViewType(newViewType: ViewType) {
        _weatherState.value = _weatherState.value.copy(viewType = newViewType)
    }

    fun searchLocation(searchedLocation: Location) {
        _weatherState.value = _weatherState.value.copy(searched = false)
        Log.d("WeatherVM", "Updated Weather: Municipality = ${searchedLocation.municipality}")
        getWeather(searchedLocation)
    }

    private fun getWeather(searchedLocation: Location) {
        viewModelScope.launch {
            _weather.value = _weather.value.updateWeather(searchedLocation)
            Log.d("WeatherVM", "Updated weather")
            _weatherState.value = _weatherState.value.copy(searched = true)
        }
    }

    init {
        viewModelScope.launch {
            _weather.value = _weather.value.updateWeather(Location())
        }
    }
}

enum class ViewType {
    Week,
    Day
}

data class WeatherState (
    val viewType: ViewType = ViewType.Day,
    val searched: Boolean = false
)