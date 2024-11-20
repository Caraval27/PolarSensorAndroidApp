package com.example.weatherapp.data

import retrofit2.*

class WeatherServerRepository {
    private val weatherApi = RetrofitClient.weatherApi

    fun fetchWeather(lonLat: String, callback: (WeatherData?) -> Unit) {
        weatherApi.getForecast(lonLat).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val fetchedData = response.body()
                    val weatherTimeData = fetchedData?.timeSeries?.map { timeSeries ->
                        val temperature = timeSeries.parameters.find { it.name == "t" }?.value?.get(0) ?: 0f
                        val symbol = timeSeries.parameters.find { it.name == "Wsymb2" }?.value?.get(0) ?: 0

                        WeatherTimeData (
                            validTime = timeSeries.validTime,
                            temperature = temperature,
                            symbol = symbol.toInt()
                        )
                    } ?: emptyList()

                    val weatherData = fetchedData?.let {
                            WeatherData (
                                approvedTime = it.approvedTime,
                                timeData = weatherTimeData
                            )
                        }

                    callback(weatherData)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}