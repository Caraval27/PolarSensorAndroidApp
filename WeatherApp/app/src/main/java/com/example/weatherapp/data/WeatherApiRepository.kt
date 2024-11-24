package com.example.weatherapp.data

import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


class WeatherApiRepository {
    private val weatherApi = RetrofitClient.weatherApi

    suspend fun fetchWeather(coordinatesData: CoordinatesData) : WeatherData? {
        return try {
            val fetchedData = weatherApi.getForecast("lon/14.333/lat/60.38")
            //val fetchedData = weatherApi.getForecast(coordinatesData.lon, coordinatesData.lat)
            Log.d("Weather", "API response successful: ${fetchedData.approvedTime}")

            val weatherTimeData = fetchedData.timeSeries.map { timeSeries ->
                val temperature = timeSeries.parameters.find { it.name == "t" }?.values?.firstOrNull()
                val symbol = timeSeries.parameters.find { it.name == "Wsymb2" }?.values?.firstOrNull()

                if (temperature == null || symbol == null) {
                    return null;
                }

                WeatherTimeData (
                    validTime = LocalDateTime.parse(timeSeries.validTime, DateTimeFormatter.ISO_DATE_TIME),
                    temperature = temperature.roundToInt(),
                    symbol = symbol.toInt()
                )
            }

            WeatherData (
                approvedTime = LocalDateTime.parse(fetchedData.approvedTime, DateTimeFormatter.ISO_DATE_TIME),
                weatherTimeData = weatherTimeData
            )
        } catch (e: Exception) {
            Log.e("Weather", "Exception occurred: ${e.localizedMessage}", e)
            null
        }
    }
}