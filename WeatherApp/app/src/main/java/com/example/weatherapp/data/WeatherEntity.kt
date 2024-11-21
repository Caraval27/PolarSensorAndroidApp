package com.example.weatherapp.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "weather",
    primaryKeys = ["locality", "municipality", "county"]
)
data class WeatherEntity (
    /*@PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = -1,*/

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
    var weather7Days: List<WeatherDayEntity> = emptyList(),

    @Ignore
    @Embedded
    var weather24Hours: List<WeatherTimeEntity> = emptyList()
)

@Entity(
    tableName = "weather_day",
    primaryKeys = ["locality", "municipality", "county", "date"],
    foreignKeys = [
        /*ForeignKey(
            entity = WeatherEntity::class,
            parentColumns = ["id"],
            childColumns = ["weather_id"],
            onDelete = ForeignKey.CASCADE
        )*/
        ForeignKey(
            entity = WeatherEntity::class,
            parentColumns = ["locality", "municipality", "county"],
            childColumns = ["locality", "municipality", "county"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class WeatherDayEntity (
    @ColumnInfo(name = "date")
    var date: String = "",

    @ColumnInfo(name = "min_temperature")
    var minTemperature: Int = Int.MAX_VALUE,

    @ColumnInfo(name = "max_temperature")
    var maxTemperature: Int = Int.MIN_VALUE,

    @ColumnInfo(name = "most_common_icon")
    var mostCommonIcon: Int = 0,

    @ColumnInfo(name = "locality")
    var locality: String = "",

    @ColumnInfo(name = "municipality")
    var municipality: String = "",

    @ColumnInfo(name = "county")
    var county: String = "",

    /*@ColumnInfo(name = "weather_id")
    val weatherId: Int*/
)

@Entity(
    tableName = "weather_time",
    primaryKeys = ["locality", "municipality", "county", "time"],
    foreignKeys = [
        /*ForeignKey(
            entity = WeatherEntity::class,
            parentColumns = ["id"],
            childColumns = ["weather_id"],
            onDelete = ForeignKey.CASCADE
        )*/
        ForeignKey(
            entity = WeatherEntity::class,
            parentColumns = ["locality", "municipality", "county"],
            childColumns = ["locality", "municipality", "county"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class WeatherTimeEntity (
    @ColumnInfo(name = "time")
    val time: String = "",

    @ColumnInfo(name = "temperature")
    var temperature: Int = Int.MIN_VALUE,

    @ColumnInfo(name = "icon")
    var icon: Int = 0,

    @ColumnInfo(name = "locality")
    var locality: String = "",

    @ColumnInfo(name = "municipality")
    var municipality: String = "",

    @ColumnInfo(name = "county")
    var county: String = "",

    /*@ColumnInfo(name = "weather_id")
    val weatherId: Int*/
)

/*data class WeatherEntityDayAndTime(
    @Embedded val weather: WeatherEntity,
    @Relation(
        parentColumn = "locality",
        entityColumn = "locality"
    )
    @Relation(
        parentColumn = "municipality",
        entityColumn = "municipality"
    )
    @Relation(
        parentColumn = "id",
        entityColumn = "weather_id"
    )
    val weather7Days: List<WeatherDayEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "weather_id"
    )
    val weather24Hours: List<WeatherTimeEntity>
)*/