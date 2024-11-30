package com.example.bluetoothapp.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bluetoothapp.presentation.components.DeviceScan
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM
import com.example.bluetoothapp.presentation.viewModel.SensorType

@Composable
fun ConnectToDeviceScreen(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    BackHandler {
        measurementVM.setSensorType(SensorType.Internal)
        navController.navigate("home")
    }

    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            DeviceScan(requestPermissionLauncher, measurementVM)
        }

        Button(
            onClick = {
                measurementVM.setSensorType(SensorType.Internal)
                navController.navigate("home")
            },
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Back to Home")
        }
    }
}