package com.example.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather/forecast")
    suspend fun getForecast(@Query("lonLat") lonLat: String): WeatherResponse
    @GET("category/pmp3g/version/2/geotype/point/lon/{lon}/lat/{lat}/data.json")
    suspend fun getForecast(@Path("lon") lon: Double, @Path("lat") lat: Double): WeatherResponse
}