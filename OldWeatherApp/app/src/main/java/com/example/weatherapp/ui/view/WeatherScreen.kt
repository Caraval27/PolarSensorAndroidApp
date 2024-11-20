package com.example.weatherapp.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.viewModel.WeatherVM

@Composable
fun WeatherScreen(
    weatherVM: WeatherVM,
) {
    val weather by weatherVM.weather.collectAsState()

    Text (
        text = "${weather.approvedTime}",
    )
}