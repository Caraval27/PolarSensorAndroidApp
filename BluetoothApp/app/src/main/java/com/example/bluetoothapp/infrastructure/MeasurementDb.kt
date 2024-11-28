package com.example.bluetoothapp.infrastructure

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MeasurementEntity::class, SampleEntity::class],
    version = 1
)
abstract class MeasurementDb : RoomDatabase() {
    abstract fun measurementDao() : MeasurementDao
}