package com.example.bluetoothapp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM
import com.example.bluetoothapp.presentation.viewModel.SensorType
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    val measurementState = measurementVM.measurementState.collectAsState()
    val isDeviceConnected by measurementVM.connectedDevice.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isDeviceConnected) {
        if (isDeviceConnected.isEmpty()) {
            snackbarHostState.showSnackbar("Polar device disconnected unexpectedly!")
        }
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
                Button(
                    onClick = { navController.navigate("connect") },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Connect to Polar sensor")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (measurementState.value.chosenDeviceId.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("No device connected. Please connect a Polar sensor first.")
                            }
                        } else {
                            measurementVM.setSensorType(SensorType.Polar)
                            measurementVM.setOngoing(true)
                            navController.navigate("plot")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Start Polar sensor")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        measurementVM.setSensorType(SensorType.Internal)
                        measurementVM.setOngoing(true)
                        navController.navigate("plot")
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Start internal sensor")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("Result history")
                }
            }
        }
    )
}