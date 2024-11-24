package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.weatherapp.model.Location

@Composable
fun LocationFormPortrait(onSubmit: (Location) -> Unit, onCancel: () -> Unit, location: Location) {
    var locality by remember { mutableStateOf(location.locality) }
    var municipality by remember { mutableStateOf(location.municipality) }
    var county by remember { mutableStateOf(location.county) }
    var localityEmpty by remember { mutableStateOf(false) }
    var municipalityEmpty by remember { mutableStateOf(false) }
    var countyEmpty by remember { mutableStateOf(false) }

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
            isError = localityEmpty,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = municipality,
            onValueChange = { municipality = it },
            label = { Text("Municipality") },
            isError = municipalityEmpty,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = county,
            onValueChange = { county = it },
            label = { Text("County") },
            isError = countyEmpty,
            singleLine = true,
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
                    localityEmpty = locality.isEmpty()
                    municipalityEmpty = municipality.isEmpty()
                    countyEmpty = county.isEmpty()
                    if (!localityEmpty && !municipalityEmpty && !countyEmpty) {
                        onSubmit(
                            Location(
                                locality = locality.trim(),
                                county = county.trim(),
                                municipality = municipality.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Select", color = Color.Blue)
            }
        }
    }
}