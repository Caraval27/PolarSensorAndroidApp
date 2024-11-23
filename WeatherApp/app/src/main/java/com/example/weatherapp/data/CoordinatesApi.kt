package com.example.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoordinatesApi {
    //@GET("weather/search")
    //suspend fun getLonLat(@Query("location") location: String): List<CoordinatesResponse>
    @GET("wpt-a/backend_solr/autocomplete/search/{location}")
    suspend fun getLonLat(@Path("location") location: String): List<CoordinatesResponse>
}