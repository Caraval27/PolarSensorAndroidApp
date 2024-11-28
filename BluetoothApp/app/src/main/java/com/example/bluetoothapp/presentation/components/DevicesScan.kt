package com.example.bluetoothapp.presentation.components

import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bluetoothapp.domain.Device
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM

@Composable
fun DeviceScan(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    val devices by measurementVM.devices.collectAsState()
    var isScanning by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) } // kanske ej behövs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (permissionDenied) {
            Text("Permissions are required to scan for devices.")
        }

        Button(
            onClick = {
                if (isScanning) {
                    isScanning = false
                } else if (measurementVM.hasRequiredPermissions()) {
                    isScanning = true
                    measurementVM.searchForDevices()
                } else {
                    requestPermissionLauncher.launch(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            arrayOf(
                                android.Manifest.permission.BLUETOOTH_SCAN,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                            )
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(
                if (isScanning) "Searching..." else "Search for Devices",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (devices.isNotEmpty()) {
            Text(
                "Select a Device",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn {
                items(devices) { device ->
                    DeviceItem(device = device, onSelect = { selectedDevice ->
                        measurementVM.connectToDevice(selectedDevice.deviceId)
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

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Back to home")
        }
    }
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
                Text(text = device.name ?: "Unknown Device", style = MaterialTheme.typography.displayMedium)
                Text(text = "ID: ${device.deviceId}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                onPermissionsResult(
                    permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                            permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
                )
            }.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                onPermissionsResult(isGranted)
            }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                onPermissionsResult(isGranted)
            }.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }*/