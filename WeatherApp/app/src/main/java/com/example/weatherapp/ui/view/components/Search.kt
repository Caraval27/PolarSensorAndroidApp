package com.example.weatherapp.ui.view.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.weatherapp.ui.viewModel.WeatherVM

@Composable
fun Search(weatherVM: WeatherVM) {
    var showDialog by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val weather by weatherVM.weather.collectAsState()
    val weatherState by weatherVM.weatherState.collectAsState()

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(115, 135, 224)
        ),
    ) {
        Text("Select location")
    }

    if (showDialog) {
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Dialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties()
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentHeight()
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Enter location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LocationFormPortrait(
                            onSubmit = { location ->
                                weatherVM.searchLocation(location)
                                showDialog = false
                            },
                            onCancel = { showDialog = false },
                            weatherState.searchedLocation
                        )
                    }
                }
            }
        } else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Dialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .wrapContentHeight()
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Enter location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LocationFormLandscape(
                            onSubmit = { location ->
                                weatherVM.searchLocation(location)
                                showDialog = false
                            },
                            onCancel = { showDialog = false },
                            weatherState.searchedLocation
                        )
                    }
                }
            }
        }
    }
}