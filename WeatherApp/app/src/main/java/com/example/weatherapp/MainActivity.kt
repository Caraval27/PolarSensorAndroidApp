package com.example.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.ui.view.WeatherScreen
import com.example.weatherapp.ui.viewModel.WeatherVM

class MainActivity : ComponentActivity() {
    private lateinit var weatherVM: WeatherVM

    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherVM = ViewModelProvider(this)[WeatherVM::class.java]
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Surface (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(129, 169, 242)),
                    color = Color.Transparent
                ) {
                    WeatherScreen(weatherVM = weatherVM)
                }
            }
        }
    }
}