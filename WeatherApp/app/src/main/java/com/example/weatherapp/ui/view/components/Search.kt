package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.model.Location
import com.example.weatherapp.ui.viewModel.WeatherVM

@Composable
fun Search(weatherVM: WeatherVM) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Select location")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enter location", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                LocationForm(
                    onSubmit = { location ->
                        weatherVM.searchLocation(location)
                        showDialog = false
                    },
                    onCancel = { showDialog = false }
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun LocationForm(onSubmit: (Location) -> Unit, onCancel: () -> Unit) {
    var locality by remember { mutableStateOf("") }
    var county by remember { mutableStateOf("") }
    var municipality by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = locality,
            onValueChange = { locality = it },
            label = { Text("Locality") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = county,
            onValueChange = { county = it },
            label = { Text("County") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = municipality,
            onValueChange = { municipality = it },
            label = { Text("Municipality") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Color.Red)
            }
            TextButton(
                onClick = {
                    onSubmit(
                        Location(
                            locality = locality,
                            county = county,
                            municipality = municipality
                        )
                    )
                }
            ) {
                Text("Submit", color = Color.Blue)
            }
        }
    }
}