package com.example.bluetoothapp.presentation.components

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.bluetoothapp.presentation.viewModel.MeasurementVM

@Composable
fun HistoryList(
    measurementVM: MeasurementVM
) {
    val measurement by measurementVM.currentMeasurement.collectAsState()
    val listState = rememberLazyListState()

}