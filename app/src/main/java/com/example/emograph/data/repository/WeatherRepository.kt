package com.example.emograph.data.repository

import com.example.emograph.data.remote.WeatherApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRepository {

    private val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    data class WeatherData(
        val description: String,
        val emoji: String,
        val temperature: Double
    )

    suspend fun getWeather(latitude: Double, longitude: Double): WeatherData {
        val response = api.getCurrentWeather(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,weathercode",
            timezone = "auto"
        )
        val code = response.current.weathercode
        return WeatherData(
            description = codeToDescription(code),
            emoji = codeToEmoji(code),
            temperature = response.current.temperature_2m
        )
    }

    private fun codeToDescription(code: Int): String = when (code) {
        0 -> "快晴"
        1 -> "ほぼ晴れ"
        2 -> "部分的に曇り"
        3 -> "曇り"
        in 45..48 -> "霧"
        in 51..53 -> "霧雨（弱）"
        in 55..57 -> "霧雨（強）"
        in 61..63 -> "雨（弱）"
        in 65..67 -> "雨（強）"
        in 71..73 -> "雪（弱）"
        in 75..77 -> "雪（強）"
        in 80..82 -> "にわか雨"
        in 85..86 -> "にわか雪"
        in 95..99 -> "雷雨"
        else -> "不明"
    }

    private fun codeToEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1 -> "🌤️"
        2 -> "⛅"
        3 -> "☁️"
        in 45..48 -> "🌫️"
        in 51..57 -> "🌦️"
        in 61..67 -> "🌧️"
        in 71..77 -> "❄️"
        in 80..82 -> "🌦️"
        in 85..86 -> "🌨️"
        in 95..99 -> "⛈️"
        else -> "🌡️"
    }
}
