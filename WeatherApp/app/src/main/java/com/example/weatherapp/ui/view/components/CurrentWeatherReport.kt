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
    val county: String = weather.location.county
    val currentTemperature: String = weather.weather24Hours.firstOrNull()?.temperature?.toString() ?: " "
    val minTemperature: String = weather.weather7Days.firstOrNull()?.minTemperature?.toString()?.plus("°") ?: " "
    val maxTemperature: String = weather.weather7Days.firstOrNull()?.maxTemperature?.toString()?.plus("°") ?: " "

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
                text = "$municipality, $county",
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentTemperature,
                fontSize = 50.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "H: $maxTemperature \t L: $minTemperature",
                fontSize = 22.sp,
                color = Color.White
            )
        }
    }
}