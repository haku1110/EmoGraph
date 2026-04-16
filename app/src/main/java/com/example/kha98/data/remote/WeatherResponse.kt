package com.kha98.emograph.data.remote

data class WeatherResponse(
    val current: CurrentWeather
)

data class CurrentWeather(
    val temperature_2m: Double,
    val weathercode: Int
)
