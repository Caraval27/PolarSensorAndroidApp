package com.example.bluetoothapp.infrastructure

import android.content.Context
import androidx.room.Room
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

    suspend fun insertMeasurement(measurement: MeasurementData) : Boolean {
        val measurementEntity = toMeasurementEntity(measurement)
        return dao.insertMeasurementWithSamples(measurementEntity)
    }

    private fun toMeasurementEntity(measurement: MeasurementData) : MeasurementEntity {
        val measurementEntity = MeasurementEntity(
            timeMeasured = measurement.timeMeasured.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        measurementEntity.sampleEntities = toSampleEntities(measurement.sampleData)
        return measurementEntity
    }

    private fun toSampleEntities(samples: List<SampleData>) : List<SampleEntity> {
        var index = 0
        return samples.map { sampleData ->
            SampleEntity(
                sequenceNumber = index++,
                singleFilteredValue = sampleData.singleFilterValue,
                fusionFilteredValue = sampleData.fusionFilterValue,
                measurementId = 0
            )
        }
    }

    suspend fun getMeasurements() : List<MeasurementData> {
        val measurementEntities = dao.getAllMeasurementsWithSamples() ?: return emptyList()
        return toMeasurementsData(measurementEntities)
    }

    private fun toMeasurementsData(measurementEntities: List<MeasurementEntity>) : List<MeasurementData> {
        return measurementEntities.map { measurementEntity ->
            measurementEntity.let {
                MeasurementData(
                    id = it.id,
                    timeMeasured = LocalDateTime.parse(it.timeMeasured, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    sampleData = toSamplesData(it.sampleEntities)
                )
            }
        }
    }

    private fun toSamplesData(sampleEntities: List<SampleEntity>) : List<SampleData> {
        return sampleEntities.map { sampleEntity ->
            SampleData(
                singleFilterValue = sampleEntity.singleFilteredValue,
                fusionFilterValue = sampleEntity.fusionFilteredValue
            )
        }
    }

    fun clearDb() {
        db.clearAllTables()
    }
}