package com.example.weatherapp.ui.view.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.weatherapp.model.Location

@Composable
fun LocationForm(
    location: Location,
    onLocationChange: (Location) -> Unit,
    onSubmit: (Location) -> Unit,
    onCancel: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    var localityEmpty by remember { mutableStateOf(false) }
    var municipalityEmpty by remember { mutableStateOf(false) }
    var countyEmpty by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(if (isPortrait) 8.dp else 16.dp)
    ) {
        if (isPortrait) {
            TextField(
                value = location.locality,
                onValueChange = {
                    onLocationChange(location.copy(locality = it))
                },
                label = { Text("Locality",  style = TextStyle(fontFamily = FontFamily.Monospace)) },
                isError = localityEmpty,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = location.municipality,
                onValueChange = {
                    onLocationChange(location.copy(municipality = it))
                },
                label = { Text("Municipality",  style = TextStyle(fontFamily = FontFamily.Monospace)) },
                isError = municipalityEmpty,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = location.county,
                onValueChange = {
                    onLocationChange(location.copy(county = it))
                },
                label = { Text("County", style = TextStyle(fontFamily = FontFamily.Monospace)) },
                isError = countyEmpty,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = location.locality,
                    onValueChange = {
                        onLocationChange(location.copy(locality = it))
                    },
                    label = { Text("Locality", style = TextStyle(fontFamily = FontFamily.Monospace)) },
                    isError = localityEmpty,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                TextField(
                    value = location.municipality,
                    onValueChange = {
                        onLocationChange(location.copy(municipality = it))
                    },
                    label = { Text("Municipality", style = TextStyle(fontFamily = FontFamily.Monospace)) },
                    isError = municipalityEmpty,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                TextField(
                    value = location.county,
                    onValueChange = {
                        onLocationChange(location.copy(county = it))
                    },
                    label = { Text("County", style = TextStyle(fontFamily = FontFamily.Monospace)) },
                    isError = countyEmpty,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDE6D6D),
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Cancel",  style = TextStyle(fontFamily = FontFamily.Monospace))
            }

            Button(
                onClick = {
                    localityEmpty = location.locality.isEmpty()
                    municipalityEmpty = location.municipality.isEmpty()
                    countyEmpty = location.county.isEmpty()
                    if (!localityEmpty && !municipalityEmpty && !countyEmpty) {
                        onSubmit(
                            Location(
                                locality = location.locality.trim(),
                                county = location.county.trim(),
                                municipality = location.municipality.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(115, 135, 224),
                    contentColor = Color.White
                )
            ) {
                Text("Search",  style = TextStyle(fontFamily = FontFamily.Monospace))
            }
        }

        /*
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Color.Red, style = TextStyle(fontFamily = FontFamily.Monospace))
            }
            TextButton(
                onClick = {
                    localityEmpty = location.locality.isEmpty()
                    municipalityEmpty = location.municipality.isEmpty()
                    countyEmpty = location.county.isEmpty()
                    if (!localityEmpty && !municipalityEmpty && !countyEmpty) {
                        onSubmit(
                            Location(
                                locality = location.locality.trim(),
                                county = location.county.trim(),
                                municipality = location.municipality.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Select", color = Color.Blue, style = TextStyle(fontFamily = FontFamily.Monospace))
            }
        }*/
    }
}