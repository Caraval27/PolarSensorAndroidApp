package com.example.weatherapp.ui.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.weatherapp.model.ErrorType
import com.example.weatherapp.model.Location
import com.example.weatherapp.ui.view.components.CurrentWeatherReport
import com.example.weatherapp.ui.view.components.NoWeatherDataAvailableLandscape
import com.example.weatherapp.ui.view.components.NoWeatherDataAvailableProfile
import com.example.weatherapp.ui.view.components.Search
import com.example.weatherapp.ui.viewModel.WeatherVM
import com.example.weatherapp.ui.view.components.WeatherReportList
import com.example.weatherapp.ui.view.components.WeatherViewTypeSelector

@Composable
fun WeatherScreen(
    weatherVM: WeatherVM,
) {
    val configuration = LocalConfiguration.current
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val locationState = rememberSaveable(stateSaver = Location.Saver) {mutableStateOf(Location())}

    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> PortraitLayout(
            weatherVM = weatherVM,
            showDialog = showDialog,
            setShowDialog = { showDialog = it },
            location = locationState.value,
            setLocation = { locationState.value = it }
        )
        Configuration.ORIENTATION_LANDSCAPE -> LandscapeLayout(
            weatherVM = weatherVM,
            showDialog = showDialog,
            setShowDialog = { showDialog = it },
            location = locationState.value,
            setLocation = { locationState.value = it }
        )
    }
}

@Composable
fun PortraitLayout(
    weatherVM: WeatherVM,
    showDialog: Boolean,
    setShowDialog: (Boolean) -> Unit,
    location: Location,
    setLocation: (Location) -> Unit
) {
    val weather by weatherVM.weather.collectAsState()
    val weatherState by weatherVM.weatherState.collectAsState()
    val snackbarHostState = SnackbarHostState()

    LaunchedEffect(weatherState.searched) {
        if (weatherState.searched) {
            val errorMessage = when (weather.errorType) {
                ErrorType.NoConnection -> "No internet connection"
                ErrorType.NoCoordinates -> "Location not found"
                ErrorType.NoWeather -> "Weather data not found"
                ErrorType.None -> return@LaunchedEffect
            }
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFFDE6D6D),
            )}},
        containerColor = Color.Transparent,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Search(
                        weatherVM = weatherVM,
                        showDialog = showDialog,
                        setShowDialog = setShowDialog,
                        location = location,
                        setLocation = setLocation
                    )
                }
                if (weather.weather7Days.isNotEmpty() && weather.weather24Hours.isNotEmpty()) {
                    CurrentWeatherReport(weather = weather)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeatherViewTypeSelector(
                            currentViewType = weatherState.viewType,
                            onViewTypeChange = { newViewType ->
                                weatherVM.updateViewType(newViewType)
                            }
                        )
                    }

                    WeatherReportList(weatherVM = weatherVM)
                } else {
                    NoWeatherDataAvailableProfile()
                }
            }
        }
    )
}

@Composable
fun LandscapeLayout(
    weatherVM: WeatherVM,
    showDialog: Boolean,
    setShowDialog: (Boolean) -> Unit,
    location: Location,
    setLocation: (Location) -> Unit
) {
    val weather by weatherVM.weather.collectAsState()
    val weatherState by weatherVM.weatherState.collectAsState()
    val snackbarHostState = SnackbarHostState()

    LaunchedEffect(weatherState.searched) {
        if (weatherState.searched) {
            val errorMessage = when (weather.errorType) {
                ErrorType.NoConnection -> "No internet connection"
                ErrorType.NoCoordinates -> "Location not found"
                ErrorType.NoWeather -> "Weather data not found"
                ErrorType.None -> return@LaunchedEffect
            }
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color.Red,
            )}},
        containerColor = Color.Transparent,
        content = { padding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (weather.weather7Days.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Search(
                                weatherVM = weatherVM,
                                showDialog = showDialog,
                                setShowDialog = setShowDialog,
                                location = location,
                                setLocation = setLocation
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        CurrentWeatherReport(weather = weather)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WeatherViewTypeSelector(
                                currentViewType = weatherState.viewType,
                                onViewTypeChange = { newViewType ->
                                    weatherVM.updateViewType(newViewType)
                                }
                            )
                        }

                        WeatherReportList(weatherVM = weatherVM)
                    }
                } else {
                    NoWeatherDataAvailableLandscape(
                        weatherVM = weatherVM,
                        showDialog = showDialog,
                        setShowDialog = setShowDialog,
                        location = location,
                        setLocation = setLocation
                    )
                }
            }
        }
    )
}