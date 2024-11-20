package com.example.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather/forecast")
    suspend fun getForecast(@Query("lonLat") lonLat: String): WeatherResponse
}