package com.example.weatherapp.data

import android.util.Log
import com.example.weatherapp.model.Location

class CoordinatesApiRepository {
    private val coordinatesApi = RetrofitClient.coordinatesApi

    suspend fun fetchCoordinates(location: Location) : CoordinatesData? {
        return try {
            val fetchedData = coordinatesApi.getLonLat(location.locality)
            val locationData = fetchedData.find {
                it.place.equals(location.locality, ignoreCase = true)
                        && it.municipality.equals(location.municipality, ignoreCase = true)
                        && it.county.equals(location.county, ignoreCase = true) }

            if (locationData == null) {
                Log.d("Coordinates", "Place not found")
                return null
            }

            val lon = kotlin.math.round(locationData.lon * 1000000) / 1000000
            val lat = kotlin.math.round(locationData.lat * 1000000) / 1000000

            Log.d("Coordinates", "API response successful: lon = $lon lat = $lat")

            CoordinatesData (
                lon = lon,
                lat = lat,
                location = Location(locality = locationData.place,
                    municipality = locationData.municipality, county = locationData.county)
            )
        } catch (e: Exception) {
            Log.e("Coordinates", "Exception occurred: ${e.localizedMessage}", e)
            null
        }
    }
}