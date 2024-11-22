package com.example.weatherapp.ui.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.Location
import com.example.weatherapp.model.Weather
import com.example.weatherapp.model.WeatherDay
import com.example.weatherapp.model.WeatherTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
        _weatherState.value = _weatherState.value.copy(selectedLocation = searchedLocation)
        Log.d("WeatherVM", "Updated Weather: Municipality = ${_weatherState.value.selectedLocation.municipality}")
        getWeather()
    }

    private fun getWeather() {
        viewModelScope.launch {
            _weather.value = _weather.value.getWeather(_weatherState.value.selectedLocation)
            Log.d("WeatherVM", "Updated weather")
        }
    }

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
    val selectedLocation: Location = Location("Sigfridstorp", "Dalarnas län", "Vansbro") // för test
    //val selectedLocation: Location = Location("Flemingsberg", "Stockholm", "Huddinge kommun")
)

/*
class FakeVM: WeatherVM() {
    private val _weather = MutableStateFlow(
        Weather(
            _location = Location("Stockholm", "Stockholm", "Huddinge kommun"),
            _approvedTime = "2024-11-21T10:00:00Z",
            _weather7Days = listOf(
                WeatherDay(
                    date = LocalDate.of(2024, 11, 21),
                    minTemperature = -2,
                    maxTemperature = 5,
                    mostCommonIcon = 1
                )
            ),
            _weather24Hours = listOf(
                WeatherTime(
                    time = LocalTime.of(6, 0),
                    temperature = -1,
                    icon = 1
                ),
                WeatherTime(
                    time = LocalTime.of(12, 0),
                    temperature = 3,
                    icon = 2
                ),
                WeatherTime(
                    time = LocalTime.of(18, 0),
                    temperature = -2,
                    icon = 3
                )
            )
        )
    )
    override val weather: StateFlow<Weather>
        get() = _weather.asStateFlow()

    private val _weatherState = MutableStateFlow(WeatherState())
    override val weatherState: StateFlow<WeatherState>
        get() = _weatherState.asStateFlow()
}
 */