package com.example.weatherapp.ui.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
            weatherVM.setSearched(false)
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
                    Spacer(modifier = Modifier.weight(1f))
                    Search(
                        weatherVM = weatherVM,
                        showDialog = showDialog,
                        setShowDialog = setShowDialog,
                        location = location,
                        setLocation = setLocation,
                        onFormOpened = { snackbarHostState.currentSnackbarData?.dismiss() }
                    )
                    Spacer(modifier = Modifier.weight(0.2f))
                    IconButton(onClick = { weatherVM.refreshWeather() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Weather",
                            tint = Color(84, 106, 235)
                        )
                    }
                }
                CurrentWeatherReport(weather = weather)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherViewTypeSelector(
                        currentViewType = weatherState.viewType,
                        onViewTypeChange = { newViewType ->
                            weatherVM.setViewType(newViewType)
                        }
                    )
                }

                WeatherReportList(weatherVM = weatherVM)
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
            weatherVM.setSearched(false)
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
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                        IconButton(onClick = { weatherVM.refreshWeather() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Weather",
                                tint = Color(84, 106, 235)
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.6f))
                        Search(
                            weatherVM = weatherVM,
                            showDialog = showDialog,
                            setShowDialog = setShowDialog,
                            location = location,
                            setLocation = setLocation,
                            onFormOpened = { snackbarHostState.currentSnackbarData?.dismiss() }
                        )
                        Spacer(modifier = Modifier.weight(1f))
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
                                weatherVM.setViewType(newViewType)
                            }
                        )
                    }

                    WeatherReportList(weatherVM = weatherVM)
                }
            }
        }
    )
}