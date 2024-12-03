package com.example.bluetoothapp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM
import com.example.bluetoothapp.presentation.viewModel.RecordingState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    val measurementHistory by measurementVM.measurementHistory.collectAsState()

    LaunchedEffect(Unit) {
        measurementVM.getMeasurementHistory()
    }

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

        if (measurementHistory.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(measurementHistory) { measurement ->
                    MeasurementItem(
                        measuredTime = measurement.timeMeasured,
                        onClick = {
                            measurementVM.setMeasurement(measurement)
                            measurementVM.setRecordingState(RecordingState.Done)
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
                    text = "No history available",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun MeasurementItem(
    measuredTime: LocalDateTime,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(169, 114, 218, 255),
            contentColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Created: " + measuredTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}