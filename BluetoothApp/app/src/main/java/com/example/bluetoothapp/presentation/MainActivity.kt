package com.example.bluetoothapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bluetoothapp.presentation.screens.ConnectToDeviceScreen
import com.example.bluetoothapp.presentation.theme.BluetoothAppTheme
import com.example.bluetoothapp.presentation.screens.HomeScreen
import com.example.bluetoothapp.presentation.screens.HistoryScreen
import com.example.bluetoothapp.presentation.screens.PlotScreen
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM

class MainActivity : ComponentActivity() {
    private lateinit var measurementVM: MeasurementVM
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        measurementVM = ViewModelProvider(this)[MeasurementVM::class.java]
        enableEdgeToEdge()

        setContent {
            BluetoothAppTheme {
                Surface (
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController : NavHostController = rememberNavController();
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(measurementVM = measurementVM, navController = navController)
                        }
                        composable("connect") {
                            ConnectToDeviceScreen(requestPermissionLauncher = requestPermissionLauncher, measurementVM = measurementVM, navController = navController)
                        }
                        composable("plot") {
                            PlotScreen(measurementVM = measurementVM, navController = navController)
                        }
                        composable("history") {
                            HistoryScreen(measurementVM = measurementVM, navController = navController)
                        }
                    }
                }
            }
        }
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {}
    }
}