package com.example.bluetoothapp.model

import android.content.Context
import com.example.bluetoothapp.data.MeasurementDbRepository

class MeasurementHistory(
    private var _measurementHistory: MutableList<Measurement> = mutableListOf(),
    private var _applicationContext: Context
){

    val measurementHistory: MutableList<Measurement>
        get() = _measurementHistory

    private val measurementDbRepository : MeasurementDbRepository = MeasurementDbRepository(_applicationContext)

    suspend fun getMeasurementsHistory() : MeasurementHistory {
        val measurementsData = measurementDbRepository.getMeasurements()

        if (measurementsData.isNotEmpty()) {
            return MeasurementHistory(
                _measurementHistory = measurementsData.map { measurementData ->
                    measurementData.let { data ->
                        Measurement(
                            _id = data.id,
                            _measured = data.timeMeasured,
                            _linearFilteredSamples = data.sampleData.map { it.singleFilterValue }
                                .toMutableList(),
                            _fusionFilteredSamples = data.sampleData.map { it.fusionFilterValue }
                                .toMutableList(),
                            _applicationContext = _applicationContext,
                            _finished = true
                        )
                    }
                }.toMutableList(),
                _applicationContext = _applicationContext
            )
        } else {
            return MeasurementHistory(mutableListOf(), _applicationContext)
        }
    }
}