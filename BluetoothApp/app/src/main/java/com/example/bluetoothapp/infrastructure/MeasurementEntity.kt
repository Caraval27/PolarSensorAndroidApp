package com.example.bluetoothapp.infrastructure

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurement",
)

data class MeasurementEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "time_measured")
    var timeMeasured: String = "",

    @Ignore
    @Embedded
    var singleFilteredSamples: List<SampleEntity> = emptyList(),

    @Ignore
    @Embedded
    var fusionFilteredSamples: List<SampleEntity> = emptyList(),
)

@Entity(
    tableName = "sample",
    primaryKeys = ["time_stamp", "filter_type", "measurement_id"],
    foreignKeys = [
        ForeignKey(
            entity = MeasurementEntity::class,
            parentColumns = ["id"],
            childColumns = ["measurement_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)

data class SampleEntity(
    @ColumnInfo(name = "time_stamp")
    var timeStamp: Long = -1,

    @ColumnInfo(name = "value")
    var value: Float = -1f,

    @ColumnInfo(name = "filter_type")
    var filterType : String = "",

    @ColumnInfo(name = "measurement_id")
    var measurementId: Int = -1,
)