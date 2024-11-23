package com.example.weatherapp.data

import android.util.Log


class WeatherApiRepository {
    private val weatherApi = RetrofitClient.weatherApi

    suspend fun fetchWeather(coordinatesData: CoordinatesData) : WeatherData? {
        return try {
            val fetchedData = weatherApi.getForecast(coordinatesData.lon, coordinatesData.lat)
            Log.d("Weather", "API response successful: ${fetchedData.approvedTime}")

            val weatherTimeData = fetchedData.timeSeries.map { timeSeries ->
                val temperature = timeSeries.parameters.find { it.name == "t" }?.values?.firstOrNull()
                val symbol = timeSeries.parameters.find { it.name == "Wsymb2" }?.values?.firstOrNull()

                if (temperature == null || symbol == null) { //osäker på om jag ska returnera att hela anropet misslyckades eller endast de tider/dagar som faktiskt misslyckades
                    return null;
                }

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