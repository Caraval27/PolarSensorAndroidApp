package com.example.weatherapp.data

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CoordinatesRepository {
    private val coordinatesApi = RetrofitClient.coordinatesApi

    fun fetchCoordinates(location: String, callback: (CoordinatesData?) -> Unit) {
        coordinatesApi.getLonLat(location).enqueue(object : Callback<CoordinatesData> {
            override fun onResponse(
                call: Call<CoordinatesData>,
                response: Response<CoordinatesData>
            ) {
                if (response.isSuccessful) {
                    val coordinatesData = response.body()
                    callback(coordinatesData)
                }
            }

            override fun onFailure(call: Call<CoordinatesData>, t: Throwable) {
                callback(null)
            }
        })
    }
}