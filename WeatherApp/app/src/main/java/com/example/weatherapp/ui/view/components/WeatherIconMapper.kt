package com.example.weatherapp.ui.view.components

import com.example.weatherapp.R

fun Int.toWeatherIconRes(): Int {
    return when (this) {
        1 -> R.drawable.clear_sky
        2 -> R.drawable.nearly_clear_sky
        3 -> R.drawable.variable_cloudiness
        4 -> R.drawable.halfclear_sky
        5 -> R.drawable.cloudy_sky
        6 -> R.drawable.overcast
        7 -> R.drawable.fog
        8 -> R.drawable.light_rain_showers
        9 -> R.drawable.moderate_rain_showers
        10 -> R.drawable.heavy_rain_showers
        11 -> R.drawable.thunderstorm
        12 -> R.drawable.light_sleet_showers
        13 -> R.drawable.moderate_sleet_showers
        14 -> R.drawable.heavy_sleet_showers
        15 -> R.drawable.light_snow_showers
        16 -> R.drawable.moderate_snow_showers
        17 -> R.drawable.heavy_snow_showers
        18 -> R.drawable.light_rain
        19 -> R.drawable.moderate_rain
        20 -> R.drawable.heavy_rain
        21 -> R.drawable.thunder
        22 -> R.drawable.light_sleet
        23 -> R.drawable.moderate_sleet
        24 -> R.drawable.heavy_sleet
        25 -> R.drawable.light_snowfall
        26 -> R.drawable.moderate_snowfall
        27 -> R.drawable.heavy_snowfall
        else -> R.drawable.weather
    }
}