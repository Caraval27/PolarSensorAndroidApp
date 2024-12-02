package com.example.bluetoothapp.presentation.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import com.example.bluetoothapp.presentation.components.LineChart
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM
import com.example.bluetoothapp.presentation.viewModel.SensorType
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

    LaunchedEffect(connectedDevice) {
        if (measurementState.value.sensorType == SensorType.Polar && connectedDevice.isEmpty()) {
            snackbarHostState.showSnackbar("Polar device disconnected unexpectedly!")
            measurementVM.setOngoing(false)
        }
    }

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
                        fusionValues = measurement.value.fusionFilteredSamples,
                        ongoing = measurementState.value.ongoing
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