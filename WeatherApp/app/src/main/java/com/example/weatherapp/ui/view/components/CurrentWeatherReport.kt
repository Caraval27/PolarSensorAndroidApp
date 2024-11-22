package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.model.Weather

@Composable
fun CurrentWeatherReport(weather: Weather) {
    val locality: String = weather.location.locality
    val municipality: String = weather.location.municipality
    val currentTemperature: Int = weather.weather24Hours.firstOrNull()?.temperature ?: 0
    val minTemperature: Int = weather.weather7Days.firstOrNull()?.minTemperature ?: 0
    val maxTemperature: Int = weather.weather7Days.firstOrNull()?.maxTemperature ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = locality,
                fontSize = 30.sp,
                color = Color.White
            )

            Text(
                text = municipality,
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$currentTemperature°",
                fontSize = 50.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "H: ${maxTemperature}° \t L: ${minTemperature}°",
                fontSize = 22.sp,
                color = Color.White
            )
        }
    }
}