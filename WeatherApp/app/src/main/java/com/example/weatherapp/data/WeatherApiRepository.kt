package com.example.weatherapp.data

import android.util.Log
import java.time.ZoneId
import java.time.ZonedDateTime


class WeatherApiRepository {
    private val weatherApi = RetrofitClient.weatherApi

    suspend fun fetchWeather(coordinatesData: CoordinatesData) : WeatherData? {
        return try {
            val fetchedData = weatherApi.getForecast(coordinatesData.lon, coordinatesData.lat)
            Log.d("Weather", "API response successful: ${fetchedData.approvedTime}")

            val weatherTimeData = fetchedData.timeSeries.map { timeSeries ->
                val temperature = timeSeries.parameters.find { it.name == "t" }?.values?.firstOrNull()
                val symbol = timeSeries.parameters.find { it.name == "Wsymb2" }?.values?.firstOrNull()

                if (temperature == null || symbol == null) {
                    return null;
                }

                WeatherTimeData (
                    validTime = ZonedDateTime.parse(timeSeries.validTime).withZoneSameInstant(ZoneId.of("Europe/Stockholm")).toLocalDateTime(),
                    temperature = temperature,
                    symbol = symbol.toInt()
                )
            }

            WeatherData (
                approvedTime = ZonedDateTime.parse(fetchedData.approvedTime).withZoneSameInstant(ZoneId.of("Europe/Stockholm")).toLocalDateTime(),
                timeData = weatherTimeData
            )
        } catch (e: Exception) {
            Log.e("Weather", "Exception occurred: ${e.localizedMessage}", e)
            null
        }
    }
}