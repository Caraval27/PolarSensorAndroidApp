package com.example.bluetoothapp.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.bluetoothapp.presentation.components.LineChart
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM
import java.time.format.DateTimeFormatter

@Composable
fun PlotScreen(
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    val measurementState = measurementVM.measurementState.collectAsState()
    val measurement = measurementVM.measurement.collectAsState()
    val snackbarHostState = SnackbarHostState()
    val isDeviceConnected by measurementVM.isDeviceConnected.collectAsState()

    LaunchedEffect(Unit) {
        if(measurementState.value.ongoing) {
            measurementVM.startRecording()
        }
    }

    LaunchedEffect(measurementState.value.exported) {
        val message = when (measurementState.value.exported) {
            true -> "Done exporting CSV file"
            false -> "Failed exporting CSV file"
            null -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message = message)
            measurementVM.setExported(null)
        }
    }

    /*LaunchedEffect(isDeviceConnected) {
        Log.d("PlotScreen", "Effect launched")
        if (isDeviceConnected) {
            Log.d("PlotScreen", "Recording started")
            measurementVM.startRecording()
        }

    }

    if (!isDeviceConnected) {
        Log.d("PlotScreen", "If")
        measurementVM.isDeviceConnected
    }*/

    BackHandler {
        if (measurementState.value.ongoing) {
            measurementVM.stopRecording()
        }
        measurementVM.setExported(null)
        navController.navigate("home")
    }

    Scaffold (
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var headerText = ""
                if (measurementState.value.ongoing) {
                    headerText = "Measuring..."
                } else {
                    headerText = measurement.value.timeMeasured.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontFamily = FontFamily.Monospace
                )

                if (measurement.value.linearFilteredSamples.isNotEmpty() && measurement.value.fusionFilteredSamples.isNotEmpty()) {
                    LineChart(
                        linearValues = measurement.value.linearFilteredSamples,
                        fusionValues = measurement.value.fusionFilteredSamples
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (measurementState.value.ongoing) {
                    Button(
                        onClick = { measurementVM.stopRecording() },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text("Stop")
                    }
                } else {
                    Button(
                        onClick = { measurementVM.exportMeasurement() },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text("Export values")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (measurementState.value.ongoing) {
                            measurementVM.stopRecording()
                        }
                        measurementVM.setExported(null)
                        navController.navigate("home")
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Home")
                }
            }
        }
    )
}