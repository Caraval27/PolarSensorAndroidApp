package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherTime

@Composable
fun WeatherReportTimeRow(weatherTime: WeatherTime) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weatherTime.time.toString(),
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.sun), // byt ut sun med icon i weathertime varje bild kommer har siffran som namn
            contentDescription = "Weather Icon",
            modifier = Modifier
                .size(40.dp)
                .weight(0.2f)
        )
        Text(
            text = "${weatherTime.temperature}°C",
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            textAlign = TextAlign.End
        )
    }
}
