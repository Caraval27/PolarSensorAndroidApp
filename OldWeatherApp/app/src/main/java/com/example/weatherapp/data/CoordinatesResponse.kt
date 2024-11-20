package com.example.weatherapp.data

data class CoordinatesResponse (
    val locations: List<LocationResponse>
)

data class LocationResponse (
    val municipality: String,
    val county: String,
    val lon: String,
    val lat: String
)