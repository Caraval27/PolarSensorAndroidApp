package com.example.weatherapp.data

import android.util.Log
import com.example.weatherapp.model.Location

class CoordinatesApiRepository {
    private val coordinatesApi = RetrofitClient.coordinatesApi

    suspend fun fetchCoordinates(location: Location) : CoordinatesData? {
        return try {
            val fetchedData = coordinatesApi.getLonLat(location.locality)
            val locationData = fetchedData.find { it.municipality == location.municipality /*&& it.county == location.county*/ }

            if (locationData == null) {
                return null;
            }

            val lon = locationData.lon
            val lat = locationData.lat

            Log.d("Coordinates", "API response successful: lon = $lon lat = $lat")

            CoordinatesData (
                lon = lon,
                lat = lat
            )
        } catch (e: Exception) {
            Log.e("Coordinates", "Exception occurred: ${e.localizedMessage}", e)
            null
        }
    }
}