package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.weatherapp.R
import com.example.weatherapp.model.ErrorType
import com.example.weatherapp.model.Weather

@Composable
fun InternetConnectionIcon(weather: Weather) {
    if (weather.errorType != ErrorType.NoConnection) {
        Image(
            painter = painterResource(id = R.drawable.internet_connection),
            contentDescription = "Internet icon",
            modifier = Modifier
                .size(30.dp)
            )
    } else {
        Image(
            painter = painterResource(id = R.drawable.no_internet_connection),
            contentDescription = "No internet icon",
            modifier = Modifier
                .size(30.dp)
        )
    }
}