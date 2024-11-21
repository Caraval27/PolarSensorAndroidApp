package com.example.weatherapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val COORDINATES_SERVER_TEST = "https://maceo.sth.kth.se/"
    private const val WEATHER_SERVER_TEST = "https://maceo.sth.kth.se/"
    private const val COORDINATES_SERVER = "https://geocode.maps.co/"
    private const val WEATHER_SERVER = " "

    val coordinatesClient: Retrofit = Retrofit.Builder()
        .baseUrl(COORDINATES_SERVER_TEST)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherClient: Retrofit = Retrofit.Builder()
        .baseUrl(WEATHER_SERVER_TEST)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val coordinatesApi: CoordinatesApi = coordinatesClient.create(CoordinatesApi::class.java)
    val weatherApi : WeatherApi = weatherClient.create(WeatherApi::class.java)
}