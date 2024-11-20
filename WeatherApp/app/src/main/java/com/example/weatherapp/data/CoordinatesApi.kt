package com.example.weatherapp.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CoordinatesApi {
    @GET("weather/search")
    fun getLonLat(@Query("location") location: String): Call<CoordinatesData>
}