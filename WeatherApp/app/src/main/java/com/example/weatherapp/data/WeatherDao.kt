package com.example.weatherapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.weatherapp.model.Weather

@Dao
interface WeatherDao {

    @Insert
    suspend fun insert(weather: Weather)

    @Query("SELECT * FROM weather WHERE location = :location")
    suspend fun getWeatherByLocation(location: String): Weather

    @Update
    suspend fun update(weather: Weather)
}