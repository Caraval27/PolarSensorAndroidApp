package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.viewModel.ViewType

@Composable
fun WeatherViewTypeSwitch(
    currentViewType: ViewType,
    onViewTypeChange: (ViewType) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 10.dp)
    ) {
        Text(
            text = if (currentViewType == ViewType.Day) "Day View" else "Week View",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp)
        )
        Switch(
            checked = currentViewType == ViewType.Week,
            onCheckedChange = { isChecked ->
                onViewTypeChange(if (isChecked) ViewType.Week else ViewType.Day)
            }
        )
    }
}