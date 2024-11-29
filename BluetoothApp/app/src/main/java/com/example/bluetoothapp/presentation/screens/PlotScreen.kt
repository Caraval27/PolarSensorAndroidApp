package com.example.bluetoothapp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bluetoothapp.presentation.components.LineChart
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM

@Composable
fun PlotScreen(
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    val measurementState = measurementVM.measurementState.collectAsState()
    val linearFilteredSamples = measurementVM.linearFilteredSamples.collectAsState()
    val fusionFilteredSamples = measurementVM.fusionFilteredSamples.collectAsState()

    LaunchedEffect(Unit) {
        measurementVM.startRecording()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
            //.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Live sensor data",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (linearFilteredSamples.value.isNotEmpty() && fusionFilteredSamples.value.isNotEmpty()) {
            LineChart(
                linearValues = linearFilteredSamples.value,
                fusionValues = fusionFilteredSamples.value
            )
        }

        Text(
            text = "Algo 1: " + linearFilteredSamples.value.lastOrNull() //Ska tas bort sen, för test
        )

        Text(
            text = "Algo 2: " + fusionFilteredSamples.value.lastOrNull() //Ska också tas bort sen
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (measurementState.value.ongoing) {
            Button(
                onClick = { measurementVM.stopRecording() },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Stop")
            }
        }
        else {
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Export values")
            }
        }
    }
}