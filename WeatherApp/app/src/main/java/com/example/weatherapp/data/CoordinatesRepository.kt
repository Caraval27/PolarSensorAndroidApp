package com.example.weatherapp.data

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CoordinatesRepository {
    private val coordinatesApi = RetrofitClient.coordinatesApi

    fun fetchCoordinates(locality: String, displayName: String, callback: (CoordinatesData?) -> Unit) {
        coordinatesApi.getLonLat(locality).enqueue(object : Callback<CoordinatesResponse> {
            override fun onResponse(
                call: Call<CoordinatesResponse>,
                response: Response<CoordinatesResponse>
            ) {
                if (response.isSuccessful) {
                    val fetchedData = response.body()
                    val locationData = fetchedData?.locations?.find { it.display_name.contains(displayName, ignoreCase = true) }
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
    }
}