package com.ai.egret.models


import com.squareup.moshi.Json

data class ForecastResponse(
    val cod: String?,
    val message: Int?,
    val cnt: Int?,
    val list: List<ForecastItem> = emptyList()
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather> = emptyList(),
    val clouds: Clouds? = null,
    val wind: Wind? = null,
    val visibility: Int? = null,
    val pop: Double = 0.0,
    @Json(name = "rain") val rain: Map<String, Double>? = null,
    @Json(name = "dt_txt") val dtTxt: String
)

data class Main(
    val temp: Double,
    @Json(name = "temp_min") val tempMin: Double,
    @Json(name = "temp_max") val tempMax: Double,
    val pressure: Int? = null,
    val humidity: Int? = null
)

data class Weather(val id: Int, val main: String, val description: String, val icon: String?)
data class Clouds(val all: Int?)
data class Wind(val speed: Double?, val deg: Int?)

// aggregated summary for UI
data class DailySummary(
    val date: String,           // "yyyy-MM-dd"
    val minTemp: Double,
    val maxTemp: Double,
    val avgTemp: Double,
    val totalRainMm: Double,
    val maxPop: Double,         // 0..1
    val predominantWeather: String,
    val maxWind: Double
)
