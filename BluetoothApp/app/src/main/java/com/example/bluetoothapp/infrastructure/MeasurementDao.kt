package com.example.bluetoothapp.infrastructure

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.bluetoothapp.domain.FilterType

@Dao
interface MeasurementDao {
    @Transaction
    suspend fun insertMeasurementWithSamples(measurementEntity: MeasurementEntity) : Boolean {
        try {
            val generatedId = insertMeasurement(measurementEntity).toInt()
            measurementEntity.singleFilteredSamples.forEach{ it.measurementId = generatedId }
            measurementEntity.fusionFilteredSamples.forEach { it.measurementId = generatedId }
            insertMeasurementSamples(measurementEntity.singleFilteredSamples)
            insertMeasurementSamples(measurementEntity.fusionFilteredSamples)
            return true
        } catch (exception: Exception) {
            Log.e("MeasurementDao", "Exception occurred: ", exception)
            return false
        }
    }

    @Insert
    suspend fun insertMeasurement(measurementEntity: MeasurementEntity) : Long

    @Insert
    suspend fun insertMeasurementSamples(sampleEntities: List<SampleEntity>)

    @Transaction
    suspend fun getAllMeasurementsWithSamples() : List<MeasurementEntity>? {
        return try {
            val measurements = getAllMeasurements()
            measurements?.forEach { measurement ->
               measurement.let {
                   it.singleFilteredSamples = getSamplesByMeasurementIdAndSensorType(it.id, FilterType.Single.toString())
                   it.fusionFilteredSamples = getSamplesByMeasurementIdAndSensorType(it.id, FilterType.Fusion.toString())
               }
            }
            measurements ?: emptyList()
        } catch (exception: Exception) {
            Log.e("MeasurementDao", "Exception occurred: ", exception)
            emptyList()
        }
    }

    @Query("""
        SELECT *
        FROM measurement
        ORDER BY time_measured DESC
    """)
    suspend fun getAllMeasurements(): List<MeasurementEntity>?

    @Query("""
        SELECT *
        FROM sample
        WHERE measurement_id = :measurementId AND filter_type = :filterType
        ORDER BY sequence_number ASC
    """)
    suspend fun getSamplesByMeasurementIdAndSensorType(measurementId: Int, filterType: String): List<SampleEntity>
}