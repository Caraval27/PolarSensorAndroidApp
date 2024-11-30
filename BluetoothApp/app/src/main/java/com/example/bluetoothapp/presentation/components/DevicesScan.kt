package com.example.bluetoothapp.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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

@SuppressLint("MissingPermission")
@Composable
fun DeviceScan(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    measurementVM: MeasurementVM,
) {
    val measurementState = measurementVM.measurementState.collectAsState()
    val devices by measurementVM.devices.collectAsState()
    var isScanning by remember { mutableStateOf(false) }
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val activity = LocalContext.current as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                            //measurementVM.stopScanning() // Ensure scanning stops
                        } else if ((bluetoothAdapter != null && bluetoothAdapter.isEnabled) && measurementVM.hasRequiredPermissions()) {
                            isScanning = true
                            measurementVM.searchForDevices()
                        } else {
                            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                activity?.startActivityForResult(enableBtIntent, 1)
                            } else {
                                requestPermissionLauncher.launch(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        arrayOf(
                                            Manifest.permission.BLUETOOTH_SCAN,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        )
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                                    } else {
                                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(
                        if (isScanning) "Stop scanning" else "Search for devices",
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
                        "Select a Device",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(devices) { device ->
                            DeviceItem(device = device, onSelect = { selectedDevice ->
                                isScanning = false
                                scope.launch {
                                    if (measurementVM.measurementState.value.chosenDeviceId.isNotEmpty()) {
                                        measurementVM.disconnectFromDevice(measurementVM.measurementState.value.chosenDeviceId)
                                        snackbarHostState.showSnackbar(message = "Disconnected from device: ${measurementState.value.chosenDeviceId}")
                                    } else {
                                        measurementVM.connectToDevice(selectedDevice.deviceId)
                                        snackbarHostState.showSnackbar(message = "Connected to device: ${selectedDevice.deviceId}")
                                    }
                                }
                            })
                        }
                    }
                } else if (!isScanning) {
                    Text(
                        "No devices found. \nPress search to start scanning.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    )
}

@Composable
fun DeviceItem(device: Device, onSelect: (Device) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect(device) },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = device.name ?: "Unknown Device", style = MaterialTheme.typography.bodyLarge)
                Text(text = "ID: ${device.deviceId}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}