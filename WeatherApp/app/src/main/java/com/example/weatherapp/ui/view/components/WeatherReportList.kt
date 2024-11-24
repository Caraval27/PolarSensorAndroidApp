package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.viewModel.ViewType
import com.example.weatherapp.ui.viewModel.WeatherVM

@Composable
fun WeatherReportList(
    weatherVM: WeatherVM
) {
    val weatherState by weatherVM.weatherState.collectAsState()
    val weather by weatherVM.weather.collectAsState()
    val weather24Hours = weather.weather24Hours
    val weather7Days = weather.weather7Days
    val listState = rememberLazyListState()

    LaunchedEffect(weatherState.viewType) {
        listState.animateScrollToItem(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 6.dp)
            .background(
                Color(174, 200, 247),
                shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (weatherState.viewType == ViewType.Day && weather24Hours.isNotEmpty()) {
                items(weather24Hours) { weatherTime ->
                    WeatherReportTimeRow(weatherTime = weatherTime)
                }
            } else if (weatherState.viewType == ViewType.Week && weather7Days.isNotEmpty()) {
                items(weather7Days) { weatherDay ->
                    WeatherReportDayRow(weatherDay = weatherDay)
                }
            }
        }
    }
}