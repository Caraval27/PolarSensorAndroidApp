package com.example.weatherapp.data

data class CoordinatesResponse (
    val locations: List<Location>
)

data class Location (
    val display_name: String,
    val lon: String,
    val lat: String
)