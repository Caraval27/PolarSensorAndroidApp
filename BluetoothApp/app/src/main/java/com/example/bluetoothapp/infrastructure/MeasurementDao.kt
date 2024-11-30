package com.example.bluetoothapp.infrastructure

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MeasurementDao {
    @Transaction
    suspend fun insertMeasurementWithSamples(measurementEntity: MeasurementEntity) {
        try {
            val generatedId = insertMeasurement(measurementEntity).toInt()
            measurementEntity.sampleEntities.forEach{ it.measurementId = generatedId }
            insertMeasurementSamples(measurementEntity.sampleEntities)
        } catch (e: Exception) {
            Log.e("MeasurementDao", "Exception occured: ${e.localizedMessage}", e)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurementEntity: MeasurementEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurementSamples(sampleEntities: List<SampleEntity>)

    @Transaction
    suspend fun getAllMeasurementsWithSamples() : List<MeasurementEntity>? {
        return try {
            val measurements = getAllMeasurements()
            measurements?.forEach { measurement ->
               measurement.let {
                   it.sampleEntities = getSamplesByMeasurementId(it.id)
               }
            }
            measurements ?: emptyList()
        } catch (e: Exception) {
            Log.e("MeasurementDao", "Exception occurred: ${e.localizedMessage}", e)
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
        WHERE measurement_id = :measurementId
        ORDER BY sequence_number ASC
    """)
    suspend fun getSamplesByMeasurementId(measurementId: Int): List<SampleEntity>
}