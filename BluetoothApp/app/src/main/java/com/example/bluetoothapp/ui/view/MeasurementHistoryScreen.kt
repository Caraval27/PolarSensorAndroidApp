package com.example.bluetoothapp.ui.view

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
import androidx.compose.ui.unit.dp
import com.example.bluetoothapp.ui.viewModel.MeasurementVM

@Composable
fun MeasurementHistoryScreen(
    measurementVM: MeasurementVM,
    onItemClick: (Int) -> Unit // en navcontroller som tar en till plotscreen
) {
    val measurementHistory by measurementVM.measurementHistory.collectAsState()
    val historyList = measurementHistory.measurementHistory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Measurement History",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (historyList.isNotEmpty()) {
            LazyColumn {
                items(historyList) { measurement ->
                    MeasurementItem(
                        measuredTime = measurement.measured.toString(),
                        onClick = { onItemClick(measurement.id) }
                    )
                }
            }
        } else {
            Text("No history available.")
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
