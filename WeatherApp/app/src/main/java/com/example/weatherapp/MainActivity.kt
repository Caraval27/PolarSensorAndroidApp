package com.example.weatherapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.ui.view.WeatherScreen
import com.example.weatherapp.ui.viewModel.WeatherVM

class MainActivity : AppCompatActivity() {
    private lateinit var weatherVM: WeatherVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherVM = ViewModelProvider(this).get(WeatherVM::class.java)
        setContent {
            WeatherScreen(vm = weatherVM)
        }
    }
}