package com.example.bluetoothapp.presentation.components

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bluetoothapp.domain.Device
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

@Composable
fun DeviceScan(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    measurementVM: MeasurementVM,
) {
    val measurementState = measurementVM.measurementState.collectAsState()
    val devices by measurementVM.devices.collectAsState()
    val connectedDevice by measurementVM.connectedDevice.collectAsState()
    var isScanning by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(measurementState.value.permissionsGranted) {
        if (measurementState.value.permissionsGranted == true && !isScanning) {
            isScanning = true
            measurementVM.searchForDevices()
        } else if (measurementState.value.permissionsGranted == false) {
            snackbarHostState.showSnackbar(message = "Bluetooth permissions denied")
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (isScanning) {
                            isScanning = false
                        } else {
                            if (measurementState.value.permissionsGranted != true) {
                                measurementVM.bluetoothPermissions(requestPermissionLauncher, activity)
                            } else {
                                isScanning = true
                                measurementVM.searchForDevices()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(
                        if (isScanning) "Stop scanning" else "Start scanning",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isScanning) {
                    Text(
                        "Scanning for devices...",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (devices.isNotEmpty()) {
                    Text(
                        "Select a device",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(devices) { device ->
                            val connectedDeviceId = measurementState.value.chosenDeviceId
                            DeviceItem(
                                device = device,
                                isConnected = device.deviceId == connectedDeviceId,
                                onSelect = { selectedDevice ->
                                isScanning = false
                                scope.launch {
                                    if (connectedDevice.isNotEmpty() && connectedDevice == measurementVM.measurementState.value.chosenDeviceId) {
                                        measurementVM.disconnectFromDevice(measurementVM.measurementState.value.chosenDeviceId)
                                        snackbarHostState.showSnackbar(message = "Disconnected from device: ${measurementState.value.chosenDeviceId}")
                                    }

                                    if (connectedDeviceId != selectedDevice.deviceId) {
                                        measurementVM.connectToDevice(selectedDevice.deviceId)
                                        snackbarHostState.showSnackbar(message = "Connected to device: ${selectedDevice.deviceId}")
                                    }
                                }
                            })
                        }
                    }
                } else if (!isScanning) {
                    Text(
                        "No devices available",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    )
}

@Composable
fun DeviceItem(device: Device, isConnected: Boolean, onSelect: (Device) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect(device) },
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = device.name ?: "Unknown device", style = MaterialTheme.typography.bodyLarge)
                Text(text = "ID: ${device.deviceId}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}