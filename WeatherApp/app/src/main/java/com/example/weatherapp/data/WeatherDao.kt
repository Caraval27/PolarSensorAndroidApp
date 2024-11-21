package com.example.weatherapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.weatherapp.model.Location
import com.example.weatherapp.model.Weather

@Dao
interface WeatherDao {

    @Transaction
    suspend fun insertWeatherDayAndTime(weather: WeatherEntity) {
        insertWeather(weather)
        insertWeatherDays(weather.weather7Days)
        insertWeatherTimes(weather.weather24Hours)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherDays(weatherDays: List<WeatherDayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherTimes(weatherTimes: List<WeatherTimeEntity>)

    @Transaction
    suspend fun getWeatherDayAndTimeByLocation(location: Location) : WeatherEntity? {
        val weather = getWeatherByLocation(location.locality, location.municipality, location.county)
            ?: return null
        weather.weather7Days = getWeatherDaysByLocation(location.locality, location.municipality, location.county)
        weather.weather24Hours = getWeatherTimesByLocation(location.locality, location.municipality, location.county)
        return weather
    }

    @Query("""
        SELECT *
        FROM weather
        WHERE locality = :locality AND municipality = :municipality AND county = :county
    """)
    suspend fun getWeatherByLocation(locality: String, municipality: String, county: String) : WeatherEntity?

    @Query("""
        SELECT *
        FROM weather_day
        WHERE locality = :locality AND municipality = :municipality AND county = :county
    """)
    suspend fun getWeatherDaysByLocation(locality: String, municipality: String, county: String) : List<WeatherDayEntity>

    @Query("""
        SELECT *
        FROM weather_time
        WHERE locality = :locality AND municipality = :municipality AND county = :county
    """)
    suspend fun getWeatherTimesByLocation(locality: String, municipality: String, county: String) : List<WeatherTimeEntity>

    /*@Query("""
        SELECT *
        FROM weather
        WHERE locality = :locality AND municipality = :municipality AND county = :county
    """)
    suspend fun getWeatherByLocation(locality: String, municipality: String, county: String): WeatherEntityDayAndTime

    @Query("""
        UPDATE weather 
        SET approved_time = :approvedTime
        WHERE locality = :locality AND municipality = :municipality AND county = :county
    """)
    suspend fun updateWeatherByLocation(locality: String, municipality: String, county: String,
                                        approvedTime: String)

    @Query("""
        UPDATE weather_day
        SET min_temperature = :minTemperature, max_temperature = :maxTemperature, most_common_icon = :mostCommonIcon
        WHERE date = :date AND weather_id IN (
            SELECT id FROM weather WHERE locality = :locality AND municipality = :municipality AND county = :county
        )
    """)
    suspend fun updateWeatherDayByLocation(date: String, locality: String, municipality: String,
                                           county: String, minTemperature: Int, maxTemperature: Int,
                                           mostCommonIcon: Int)

    @Query("""
        UPDATE weather_time
        SET temperature = :temperature, icon = :icon
        WHERE time = :time AND weather_id IN (
            SELECT id FROM weather WHERE locality = :locality AND municipality = :municipality AND county = :county
        )
    """)
    suspend fun updateWeatherTimeByLocation(time: String, locality: String, municipality: String,
                                            county: String, temperature: Int, icon: Int)*/
}