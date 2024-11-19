package com.example.weatherapp.data

import retrofit2.Call

class WeatherRepository {
    private val weatherApi = RetrofitClient.weatherApi

    fun fetchWeather(lonLat: String, callback: (List<WeatherData>) -> Unit) {
        weatherApi.getForecast(lonLat).enqueue(object : retrofit2.Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: retrofit2.Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    val processedData = weatherData?.timeSeries?.map { timeSeries ->
                        val temperature = timeSeries.parameters.find { it.name == "t" }?.value?.get(0) ?: 0f
                        val symbol = timeSeries.parameters.find { it.name == "Wsymb2" }?.value?.get(0) ?: 0

                        WeatherData (
                            validTime = timeSeries.validTime,
                            temperature = temperature,
                            symbol = symbol.toInt()
                        )
                    } ?: emptyList()

                    callback(processedData)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                callback(emptyList())
            }
        })
    }
}