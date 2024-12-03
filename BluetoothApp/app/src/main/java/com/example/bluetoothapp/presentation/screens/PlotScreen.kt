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
import com.example.bluetoothapp.presentation.viewModel.RecordingState
import com.example.bluetoothapp.domain.SensorType
import java.time.format.DateTimeFormatter

@Composable
fun PlotScreen(
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    val measurementState = measurementVM.measurementState.collectAsState()
    val measurement = measurementVM.measurement.collectAsState()
    val connectedDevice by measurementVM.connectedDevice.collectAsState()
    val snackbarHostState = SnackbarHostState()

    LaunchedEffect(Unit) {
        if(measurementState.value.recordingState == RecordingState.Requested) {
            measurementVM.startRecording()
        }
    }

    LaunchedEffect(measurementState.value.exported) {
        val message = when (measurementState.value.exported) {
            true -> "CSV file exported"
            false -> "Failed exporting CSV file"
            null -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message = message)
            measurementVM.setExported(null)
        }
    }

    LaunchedEffect(measurementState.value.saved) {
        val message = when (measurementState.value.saved) {
            true -> "Measurement data saved"
            false -> "Failed saving measurement data"
            null -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message = message)
            measurementVM.setSaved(null)
        }
    }

    LaunchedEffect(connectedDevice) {
        if (measurementState.value.sensorType == SensorType.Polar && connectedDevice.isEmpty()) {
            snackbarHostState.showSnackbar("Polar sensor disconnected")
            measurementVM.stopRecording()
            measurementVM.saveRecording()
        }
    }

    BackHandler {
        if (measurementState.value.recordingState != RecordingState.Requested) {
            measurementVM.stopRecording()
        }
        measurementVM.setSaved(null)
        measurementVM.setExported(null)
        navController.popBackStack()
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
                var headerText = when (measurementState.value.recordingState) {
                    RecordingState.Requested -> "Loading..."
                    RecordingState.Ongoing -> "Measuring..."
                    RecordingState.Done -> measurement.value.timeMeasured.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontFamily = FontFamily.Monospace
                )

                if (measurementState.value.recordingState != RecordingState.Requested && measurement.value.singleFilteredSamples.isNotEmpty() && measurement.value.fusionFilteredSamples.isNotEmpty()) {
                    LineChart(
                        linearValues = measurement.value.singleFilteredSamples,
                        fusionValues = measurement.value.fusionFilteredSamples,
                        recordingState = measurementState.value.recordingState
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (measurementState.value.recordingState == RecordingState.Ongoing) {
                    Button(
                        onClick = {
                            measurementVM.stopRecording()
                            measurementVM.saveRecording()
                                  },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text("Stop")
                    }
                } else if (measurementState.value.recordingState == RecordingState.Done){
                    Button(
                        onClick = {
                                measurementVM.exportMeasurement()
                             },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text("Export values")
                    }
                }
            }
        }
    )
}