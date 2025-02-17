package com.example.bluetoothapp.infrastructure

import android.content.Context
import androidx.room.Room
import com.example.bluetoothapp.domain.FilterType
import com.example.bluetoothapp.domain.Measurement
import com.example.bluetoothapp.domain.Sample
import com.example.bluetoothapp.domain.SensorType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MeasurementDbRepository(
    applicationContext: Context
) {
    private val db = Room.databaseBuilder(
        applicationContext,
        MeasurementDb::class.java, "measurement_db"
    ).build()

    private val dao = db.measurementDao()

    suspend fun insertMeasurement(measurement: Measurement) : Boolean {
        val measurementEntity = toMeasurementEntity(measurement)
        return dao.insertMeasurementWithSamples(measurementEntity)
    }

    private fun toMeasurementEntity(measurement: Measurement) : MeasurementEntity {
        return MeasurementEntity(
            timeMeasured = measurement.timeMeasured.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            sensorType = measurement.sensorType.toString(),
            singleFilteredSamples = toSampleEntities(measurement.singleFilteredSamples, FilterType.Single),
            fusionFilteredSamples = toSampleEntities(measurement.fusionFilteredSamples, FilterType.Fusion)
        )
    }

    private fun toSampleEntities(samples: List<Sample>, filterType: FilterType) : List<SampleEntity> {
        return samples.map { sample ->
            SampleEntity(
                timeStamp = sample.sequenceNumber,
                value = sample.value,
                filterType = filterType.toString()
            )
        }
    }

    suspend fun getMeasurements() : List<Measurement> {
        val measurementEntities = dao.getAllMeasurementsWithSamples() ?: return emptyList()
        return toMeasurements(measurementEntities)
    }

    private fun toMeasurements(measurementEntities: List<MeasurementEntity>) : List<Measurement> {
        return measurementEntities.map { measurementEntity ->
            measurementEntity.let {
                Measurement(
                    id = it.id,
                    timeMeasured = LocalDateTime.parse(it.timeMeasured, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    singleFilteredSamples = toSamples(it.singleFilteredSamples),
                    fusionFilteredSamples = toSamples(it.fusionFilteredSamples),
                    sensorType = SensorType.valueOf(it.sensorType)
                )
            }
        }
    }

    private fun toSamples(sampleEntities: List<SampleEntity>) : List<Sample> {
        return sampleEntities.map { sampleEntity ->
            Sample(
                value = sampleEntity.value,
                sequenceNumber = sampleEntity.timeStamp
            )
        }
    }
}