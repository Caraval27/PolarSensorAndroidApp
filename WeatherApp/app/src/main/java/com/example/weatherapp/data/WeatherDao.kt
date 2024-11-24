package com.example.weatherapp.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.weatherapp.model.Location

@Dao
interface WeatherDao {

    @Transaction
    suspend fun insertWeatherWithTime(weather: WeatherEntity) {
        try {
            insertWeather(weather)
            insertWeatherTimes(weather.weatherTimeEntities)
        }
        catch (e: Exception) {
            Log.e("WeatherDao", "Exception occurred: ${e.localizedMessage}", e)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherTimes(weatherTimes: List<WeatherTimeEntity>)

    @Transaction
    suspend fun getWeatherWithTimeByLocation(location: Location) : WeatherEntity? {
        try {
            val weather = getWeatherByLocation(location.locality, location.municipality, location.county)
                ?: return null
            weather.weatherTimeEntities = getWeatherTimesByLocation(location.locality, location.municipality, location.county)
            return weather
        }
        catch (e: Exception) {
            Log.e("WeatherDao", "Exception occurred: ${e.localizedMessage}", e)
            return null;
        }
    }

    @Query("""
        SELECT *
        FROM weather
        WHERE locality = :locality AND municipality = :municipality AND county = :county
    """)
    suspend fun getWeatherByLocation(locality: String, municipality: String, county: String) : WeatherEntity?

    @Query("""
        SELECT *
        FROM weather_time
        WHERE locality = :locality AND municipality = :municipality AND county = :county
    """)
    suspend fun getWeatherTimesByLocation(locality: String, municipality: String, county: String) : List<WeatherTimeEntity>
}