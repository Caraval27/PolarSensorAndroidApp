package com.example.weatherapp.ui.view

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.example.weatherapp.ui.viewModel.WeatherVM

@Composable
fun Test(
    vm: WeatherVM,
) {
    val test = vm.weatherData()

    Text(
        text = "High Score = $test",
    )
}