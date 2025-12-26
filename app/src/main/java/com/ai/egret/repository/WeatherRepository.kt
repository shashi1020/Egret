package com.ai.egret.repository


import com.ai.egret.models.DailySummary
import com.ai.egret.models.ForecastResponse
import com.ai.egret.network.OpenWeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.round

class WeatherRepository @Inject constructor(
    private val api: OpenWeatherApiService
) {

    suspend fun fetchForecastAndAggregate(
        lat: Double,
        lon: Double,
        apiKey: String // Pass key here from ViewModel
    ): List<DailySummary> = withContext(Dispatchers.IO) {
        // "metric" is hardcoded as per your previous logic
        val resp = api.forecast(lat, lon, "metric", apiKey)
        aggregateToDaily(resp)
    }

    // --- Your Aggregation Logic (Preserved) ---
    private fun aggregateToDaily(response: ForecastResponse): List<DailySummary> {
        // Group by Date (YYYY-MM-DD)
        val dayGroups = response.list.groupBy { it.dtTxt.substring(0, 10) }

        val summaries = dayGroups.map { (date, items) ->
            val minTemp = items.minOfOrNull { it.main.tempMin } ?: 0.0
            val maxTemp = items.maxOfOrNull { it.main.tempMax } ?: 0.0
            val avgTemp = if (items.isNotEmpty()) items.map { it.main.temp }.average() else 0.0

            // Handle Rain (checking nullability safely)
            val totalRain = items.sumOf { it.rain?.get("3h") ?: 0.0 }

            val maxPop = items.maxOfOrNull { it.pop } ?: 0.0
            val maxWind = items.mapNotNull { it.wind?.speed }.maxOrNull() ?: 0.0

            // Determine predominant weather condition
            val weatherFreq = items.flatMap { it.weather }.groupingBy { it.main }.eachCount()
            val predominant = weatherFreq.maxByOrNull { it.value }?.key
                ?: items.firstOrNull()?.weather?.firstOrNull()?.main
                ?: "Unknown"

            DailySummary(
                date = date,
                minTemp = (round(minTemp * 10) / 10.0),
                maxTemp = (round(maxTemp * 10) / 10.0),
                avgTemp = (round(avgTemp * 10) / 10.0),
                totalRainMm = (round(totalRain * 10) / 10.0),
                maxPop = (round(maxPop * 100) / 100.0),
                predominantWeather = predominant,
                maxWind = (round(maxWind * 10) / 10.0)
            )
        }

        return summaries.sortedBy { it.date }
    }
}