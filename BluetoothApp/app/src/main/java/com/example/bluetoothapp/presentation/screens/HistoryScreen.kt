package com.example.bluetoothapp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM

@Composable
fun HistoryScreen(
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    val measurementHistory by measurementVM.measurementHistory.collectAsState()

    /* testdata för endast tid
    val measurementHistory = mutableListOf<LocalDateTime>()

    measurementHistory.add(LocalDateTime.of(2024, 11, 28, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 27, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 26, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 25, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 24, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 23, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 22, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 21, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 20, 16, 20, 2, 872739))
    measurementHistory.add(LocalDateTime.of(2024, 11, 19, 16, 20, 2, 872739))
     */

    Column(
        modifier = Modifier
            .fillMaxSize(0.8f)
            .padding(16.dp),
    ) {
        Text(
            text = "Measurement history",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(30.dp),
            fontFamily = FontFamily.Monospace
        )

        if (measurementHistory.isNotEmpty()) { //measurementHistory.isNotEmpty()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(measurementHistory) { measurement ->
                    MeasurementItem(
                        measuredTime = measurement.measured.toString(),
                        onClick = {
                            measurementVM.setCurrentMeasurement(measurement)
                            navController.navigate("plot")
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history available.",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun MeasurementItem(
    measuredTime: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = measuredTime,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}