package com.example.weatherapp.data

data class CoordinatesResponse(
    val lon: Double,
    val lat: Double,
    val municipality: String,
    val county: String
)