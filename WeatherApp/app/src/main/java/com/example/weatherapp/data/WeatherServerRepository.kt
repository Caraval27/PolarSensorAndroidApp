package com.example.weatherapp.data

import android.util.Log


class WeatherServerRepository {
    private val weatherApi = RetrofitClient.weatherApi

    suspend fun fetchWeather(lonLat: String) : WeatherData? {
        return try {
            val fetchedData = weatherApi.getForecast(lonLat)
            Log.d("Weather", "API response successful: ${fetchedData.approvedTime}")

            val weatherTimeData = fetchedData.timeSeries.map { timeSeries ->
                val temperature = timeSeries.parameters.find { it.name == "t" }?.value?.get(0) ?: 0f
                val symbol = timeSeries.parameters.find { it.name == "Wsymb2" }?.value?.get(0) ?: 0

                WeatherTimeData (
                    validTime = timeSeries.validTime,
                    temperature = temperature,
                    symbol = symbol.toInt()
                )
            }

            WeatherData (
                approvedTime = fetchedData.approvedTime,
                timeData = weatherTimeData
            )
        } catch (e: Exception) {
            Log.e("Weather", "Exception occurred: ${e.localizedMessage}", e)
            null
        }
    }
}