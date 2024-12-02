package com.example.bluetoothapp.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bluetoothapp.presentation.viewModel.RecordingState
import com.github.mikephil.charting.charts.LineChart

@Composable
fun LineChart(
    linearValues: List<Float>,
    fusionValues: List<Float>,
    recordingState: RecordingState
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                setupLineChart(this, linearValues, fusionValues, recordingState)
            }
        },
        update = { chart ->
            if (linearValues.isNotEmpty() && fusionValues.isNotEmpty()) {
                chart.clear()
                setupLineChart(chart, linearValues, fusionValues, recordingState)
            }
        }
    )
}