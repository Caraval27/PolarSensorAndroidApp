package com.example.weatherapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val WEATHER_SERVER_TEST = "https://maceo.sth.kth.se/"
    private const val WEATHER_SERVER_BASE_URL = " "

    val retrofit: Retrofit = Retrofit.Builder()
         .baseUrl(WEATHER_SERVER_TEST)
         .addConverterFactory(GsonConverterFactory.create())
         .build()

    val weatherApi : WeatherApi = retrofit.create(WeatherApi::class.java)
}