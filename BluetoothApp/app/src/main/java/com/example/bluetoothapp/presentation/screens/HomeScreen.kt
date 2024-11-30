package com.example.bluetoothapp.presentation.screens

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bluetoothapp.presentation.components.DeviceScan
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM
import com.example.bluetoothapp.presentation.viewModel.SensorType

@Composable
fun HomeScreen(
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    var polarSelected by remember { mutableStateOf(false) }

    //Knappnamnen kan ändras
    //Knapp för history ska finnas, när man trycker ska den hämta history i vm
    //Knapp för internal sensor --> startar skanning direkt?
    //Knapp för bluetooth --> innehållet i home ändras till att scanna efter enheter

    if (!polarSelected) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    measurementVM.setSensorType(SensorType.Internal)
                    measurementVM.setOngoing(true)
                    navController.navigate("plot")
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Use internal sensor")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    measurementVM.setSensorType(SensorType.Polar)
                    polarSelected = true
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Use polar sensors")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("history") },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Result history")
            }
        }
    } else {
        DeviceScan(requestPermissionLauncher, measurementVM, navController)
    }
}