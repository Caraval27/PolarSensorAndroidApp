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
fun LocationFormPortrait(onSubmit: (Location) -> Unit, onCancel: () -> Unit) {
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
                Text("Search", color = Color.Blue)
            }
        }
    }
}