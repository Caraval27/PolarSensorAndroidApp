package com.example.weatherapp.ui.viewModel

import android.app.Application
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

    fun setViewType(viewType: ViewType) {
        _weatherState.value = _weatherState.value.copy(viewType = viewType)
    }

    fun setSearched(searched: Boolean) {
        _weatherState.value = _weatherState.value.copy(searched = searched)
    }

    fun searchLocation(searchedLocation: Location) {
        _weatherState.value = _weatherState.value.copy(searchedLocation = searchedLocation)
        getWeather()
    }

    private fun getWeather() {
        viewModelScope.launch {
            _weather.value = _weather.value.updateWeather(_weatherState.value.searchedLocation)
            _weatherState.value = _weatherState.value.copy(searched = true)
        }
    }

    init {
            getWeather()
       //application.applicationContext.deleteDatabase("weather_db")
    }
}

enum class ViewType {
    Week,
    Day
}

data class WeatherState (
    val viewType: ViewType = ViewType.Day,
    val searched: Boolean = false,
    val searchedLocation: Location = Location()
)