package com.example.weatherapp.data

import com.example.weatherapp.model.Location
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CoordinatesRepository {
    private val coordinatesApi = RetrofitClient.coordinatesApi

    fun fetchCoordinates(location : Location?, callback: (CoordinatesData?) -> Unit) {
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
    }
}