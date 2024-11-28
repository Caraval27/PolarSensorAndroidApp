package com.example.bluetoothapp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM

@Composable
fun PlotScreen(
    measurementVM: MeasurementVM,
    navController: NavHostController
) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Här ska man ploten vara tänker jag

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                //measurementVM.stopMeasurement()
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Stop measurement")
        }
    }

    Spacer(modifier = Modifier.height(40.dp))

    Button(
        onClick = { navController.navigate("home") },
        modifier = Modifier.fillMaxWidth(0.5f)
    ) {
        Text("Back")
    }
}