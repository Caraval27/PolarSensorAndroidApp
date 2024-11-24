package com.example.weatherapp.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore

@Entity(
    tableName = "weather",
    primaryKeys = ["locality", "municipality", "county"]
)
data class WeatherEntity (

    @ColumnInfo(name = "locality")
    var locality: String = "",

    @ColumnInfo(name = "municipality")
    var municipality: String = "",

    @ColumnInfo(name = "county")
    var county: String = "",

    @ColumnInfo(name = "approved_time")
    var approvedTime: String = "",

    @Ignore
    @Embedded
    var weatherTimeEntities: List<WeatherTimeEntity> = emptyList(),
)

@Entity(
    tableName = "weather_time",
    primaryKeys = ["locality", "municipality", "county", "valid_time"],
    foreignKeys = [
        ForeignKey(
            entity = WeatherEntity::class,
            parentColumns = ["locality", "municipality", "county"],
            childColumns = ["locality", "municipality", "county"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class WeatherTimeEntity (
    @ColumnInfo(name = "valid_time")
    val validTime: String = "",

    @ColumnInfo(name = "temperature")
    var temperature: Int = Int.MIN_VALUE,

    @ColumnInfo(name = "symbol")
    var symbol: Int = 0,

    @ColumnInfo(name = "locality")
    var locality: String = "",

    @ColumnInfo(name = "municipality")
    var municipality: String = "",

    @ColumnInfo(name = "county")
    var county: String = "",
)