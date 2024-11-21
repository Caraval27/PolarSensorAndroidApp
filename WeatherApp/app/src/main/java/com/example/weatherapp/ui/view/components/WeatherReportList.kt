package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.weatherapp.ui.viewModel.ViewType
import com.example.weatherapp.ui.viewModel.WeatherVM

@Composable
fun WeatherReportList(
    weatherVM: WeatherVM
) {
    val weatherState by weatherVM.weatherState.collectAsState()
    val weather by weatherVM.weather.collectAsState()
    val weather24Hours = weather.weather24Hours

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if (weatherState.viewType == ViewType.Day && !weather24Hours.isNullOrEmpty()) {
            items(weather24Hours) { weatherTime ->
                WeatherTimeReportRow(weatherTime = weatherTime)
            }
        } else {

            TODO() // rader för dagar
        }
    }
}