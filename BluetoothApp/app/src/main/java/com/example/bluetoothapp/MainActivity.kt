package com.example.bluetoothapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.bluetoothapp.ui.theme.BluetoothAppTheme
import com.example.bluetoothapp.ui.view.HomeScreen
import com.example.bluetoothapp.ui.viewModel.MeasurementVM

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
                        .fillMaxSize()
                        .background(Color(129, 169, 242)),
                    color = Color.Transparent
                ) {
                    HomeScreen(
                        requestPermissionLauncher = requestPermissionLauncher,
                        measurementVM = measurementVM
                    )
                }
            }
        }
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {}
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothAppTheme {
        Greeting("Android")
    }
}