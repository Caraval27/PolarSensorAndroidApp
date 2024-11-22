package com.example.weatherapp.ui.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.view.components.CurrentWeatherReport
import com.example.weatherapp.ui.view.components.Search
import com.example.weatherapp.ui.viewModel.WeatherVM
import com.example.weatherapp.ui.view.components.WeatherReportList
import com.example.weatherapp.ui.view.components.WeatherViewTypeSwitch

@Composable
fun WeatherScreen(
    weatherVM: WeatherVM,
) {
    val configuration = LocalConfiguration.current

    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> PortraitLayout(weatherVM = weatherVM)
        Configuration.ORIENTATION_LANDSCAPE -> LandscapeLayout(weatherVM = weatherVM)
    }
}

@Composable
fun PortraitLayout(
    weatherVM: WeatherVM
) {
    val weather by weatherVM.weather.collectAsState()
    val weatherState by weatherVM.weatherState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Search(weatherVM = weatherVM)
        }
        if (weather.weather7Days.isNotEmpty()) { // temporärt måste hantera tom lista
            CurrentWeatherReport(weather = weather)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeatherViewTypeSwitch(
                    currentViewType = weatherState.viewType,
                    onViewTypeChange = { newViewType ->
                        weatherVM.updateViewType(newViewType)
                    }
                )
            }

            WeatherReportList(weatherVM = weatherVM)
        }
    }
}

@Composable
fun LandscapeLayout(
    weatherVM: WeatherVM
) {
    val weather by weatherVM.weather.collectAsState()
    val weatherState by weatherVM.weatherState.collectAsState()

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
            Search(weatherVM = weatherVM)
            Spacer(modifier = Modifier.height(8.dp))
            CurrentWeatherReport(weather = weather)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (weather.weather7Days.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherViewTypeSwitch(
                        currentViewType = weatherState.viewType,
                        onViewTypeChange = { newViewType ->
                            weatherVM.updateViewType(newViewType)
                        }
                    )
                }

                WeatherReportList(weatherVM = weatherVM)
            }
        }
    }
}