package com.example.weatherapp.data

/*
data class CoordinatesResponse (
    val locations: List<LocationResponse>
)

data class LocationResponse (
    val municipality: String,
    val county: String,
    val lon: String,
    val lat: String
)
 */
data class CoordinatesResponse(
    val lon: Double,
    val lat: Double,
    val municipality: String,
    val county: String
)