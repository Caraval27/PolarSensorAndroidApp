package com.example.weatherapp.ui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.weatherapp.model.Location
import com.example.weatherapp.ui.viewModel.WeatherVM

@Composable
fun Search(
    weatherVM: WeatherVM,
    showDialog: Boolean,
    setShowDialog: (Boolean) -> Unit,
    location: Location,
    setLocation: (Location) -> Unit,
    onFormOpened : () -> Unit
) {
    Button(
        onClick = {
            setShowDialog(true)
            onFormOpened()
                  },
        modifier = Modifier.padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(115, 135, 224)
        ),
    ) {
        Text("Select location", style = TextStyle(fontFamily = FontFamily.Monospace))
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { setShowDialog(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
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
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LocationForm(
                        location = location,
                        onLocationChange = setLocation,
                        onSubmit = { location ->
                            weatherVM.searchLocation(location)
                            setShowDialog(false)
                        },
                        onCancel = { setShowDialog(false) }
                    )
                }
            }
        }
    }
}