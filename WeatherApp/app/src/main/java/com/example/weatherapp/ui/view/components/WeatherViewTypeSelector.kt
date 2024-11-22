package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.viewModel.ViewType

@Composable
fun WeatherViewTypeSelector(
    currentViewType: ViewType,
    onViewTypeChange: (ViewType) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Day view",
            fontSize = 14.sp,
            color = Color.White,
        )
        RadioButton(
            selected = currentViewType == ViewType.Day,
            onClick = { onViewTypeChange(ViewType.Day) },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color.White,
                unselectedColor = Color(115, 135, 224)
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Week view",
            fontSize = 14.sp,
            color = Color.White,
        )
        RadioButton(
            selected = currentViewType == ViewType.Week,
            onClick = { onViewTypeChange(ViewType.Week) },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color.White,
                unselectedColor = Color(115, 135, 224)
            )
        )
    }
}