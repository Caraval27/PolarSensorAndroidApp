package com.example.weatherapp.ui.view

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.weatherapp.ui.viewModel.WeatherVM

@Composable
fun WeatherScreen(
    weatherVM: WeatherVM,
) {
    val weather by weatherVM.weather.collectAsState()
    Log.d("WeatherScreen", "Observed Approved Time: ${weather.approvedTime}")

    Text (
        text = "Hej : ${weather.approvedTime ?: "No data available"}"
    )
}