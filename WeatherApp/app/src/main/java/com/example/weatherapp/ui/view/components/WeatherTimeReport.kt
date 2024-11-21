package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherTime

@Composable
fun WeatherTimeReportRow(weatherTime: WeatherTime) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weatherTime.time.toString(),
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.sun), // byt ut sun med icon i weathertime
            contentDescription = "Weather Icon",
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = "${weatherTime.temperature}°C",
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
    }
}
