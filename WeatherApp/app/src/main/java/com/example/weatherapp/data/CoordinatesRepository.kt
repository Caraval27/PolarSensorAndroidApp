package com.example.weatherapp.data

import android.util.Log
import com.example.weatherapp.model.Location

class CoordinatesRepository {
    private val coordinatesApi = RetrofitClient.coordinatesApi

    suspend fun fetchCoordinates(location: Location?) : CoordinatesData? {
        return try {
            val fetchedData = coordinatesApi.getLonLat(location?.locality)
            val locationData = fetchedData.find { it.municipality == location?.municipality /*&& it.county == location.county*/ }
            val lon = locationData?.lon
            val lat = locationData?.lat

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

    /*fun fetchCoordinates(location : Location?, callback: (CoordinatesData?) -> Unit) {
        coordinatesApi.getLonLat(location?.locality).enqueue(object : Callback<CoordinatesResponse> {
            override fun onResponse(
                call: Call<CoordinatesResponse>,
                response: Response<CoordinatesResponse>
            ) {
                if (response.isSuccessful) {
                    val fetchedData = response.body()
                    val locationData = fetchedData?.locations?.find { it.municipality == location?.municipality && it.county == location.county}
                    val lon = locationData?.lon?.toDouble()
                    val lat = locationData?.lat?.toDouble()
                    val coordinatesData = CoordinatesData(
                        lon = lon,
                        lat = lat
                    )

                    callback(coordinatesData)
                }
            }

            override fun onFailure(call: Call<CoordinatesResponse>, t: Throwable) {
                callback(null)
            }
        })
    }*/
}