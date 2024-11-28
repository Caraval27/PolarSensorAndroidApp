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
    var sampleEntities: List<SampleEntity> = emptyList(),
)

@Entity(
    tableName = "sample",
    primaryKeys = ["sequence_number", "measurement_id"],
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
    @ColumnInfo(name = "sequence_number")
    var sequenceNumber: Int = 0,

    @ColumnInfo(name = "single_filtered_value")
    var singleFilteredValue: Float = 0f,

    @ColumnInfo(name = "fusion_filtered_value")
    var fusionFilteredValue: Float = 0f,

    @ColumnInfo(name = "measurement_id")
    var measurementId: Int = 0,
)