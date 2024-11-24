package com.example.weatherapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WeatherEntity::class, WeatherTimeEntity::class],
    version = 1
)
abstract class WeatherDb : RoomDatabase() {
    abstract fun weatherDao() : WeatherDao
}