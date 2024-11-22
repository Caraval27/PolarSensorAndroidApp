package com.example.weatherapp.ui.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.*
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

    /*
    val weather by weatherVM.weather.collectAsState()
    val configuration = LocalConfiguration.current

    if (weather.weather24Hours.isNullOrEmpty()) {
        Text(text = "No weather data available")
    } else {
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> WeatherTimeList(weatherTimes = weather.weather24Hours!!)
            Configuration.ORIENTATION_LANDSCAPE -> {
                // You can create a different layout for landscape if needed
                WeatherTimeList(weatherTimes = weather.weather24Hours!!)
            }
        }
    }
     */
}
/*
@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    val fakeWeatherVM = FakeVM()

    Surface {
        WeatherScreen(weatherVM = fakeWeatherVM)
    }
}
 */