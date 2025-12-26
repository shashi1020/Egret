package com.ai.egret.repository


import android.util.Log
import com.ai.egret.data.mapper.toEntity
import com.ai.egret.models.MarketPriceEntity
import com.ai.egret.network.MarketApiService
import com.ai.egret.roomDB.MarketPriceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MarketPriceRepository @Inject constructor(
    private val api: MarketApiService,
    private val dao: MarketPriceDao
) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // --- 1. Get Data (Source of Truth: Database) ---
    // The UI should observe this. It will be empty initially, then fill up after refresh.
    suspend fun getLocalPrices(): List<MarketPriceEntity> {
        return dao.getAllPrices()
    }

    // --- 2. Refresh Logic (The Brains) ---
    suspend fun refreshMarketPrices(apiKey: String) {
        withContext(Dispatchers.IO) {
            try {
                // A. Fetch from API
                val response = api.getMarketPrices(
                    apiKey = apiKey,
                    limit = 1000 // Fetch enough to cover recent days
                )
                val rawRecords = response.records ?: emptyList()

                if (rawRecords.isNotEmpty()) {
                    // B. Calculate "3 Days Ago" Date
                    val threeDaysAgo = getThreeDaysAgoDate()

                    // C. Filter Logic (In Kotlin)
                    // We parse the string date and check if it's new enough
                    val validRecords = rawRecords.filter { record ->
                        try {
                            val recordDate = dateFormat.parse(record.arrivalDate ?: "")
                            // Keep if date is valid AND after (or equal to) 3 days ago
                            recordDate != null && !recordDate.before(threeDaysAgo)
                        } catch (e: Exception) {
                            false // discard bad dates
                        }
                    }

                    // D. Convert to Entities
                    val entities = validRecords.map { it.toEntity() }

                    // E. Efficient Storage Strategy (Wipe & Replace)
                    // Since filtering old string dates in SQL is risky, we clear the DB
                    // and insert ONLY the valid, filtered 3-day data.
                    dao.clearAllMarketPrices()
                    dao.insertAll(entities)

                    Log.d("MarketRepo", "Refreshed: Fetched ${rawRecords.size}, Stored ${entities.size} (Last 3 days)")
                }

            } catch (e: Exception) {
                Log.e("MarketRepo", "Failed to refresh prices", e)
                throw e // Let ViewModel handle the error (show Snackbar)
            }
        }
    }

    // --- Helper to get the cutoff date ---
    private fun getThreeDaysAgoDate(): Date {
        val calendar = Calendar.getInstance()
        // Reset time to midnight to ensure we get full days
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Go back 3 days
        calendar.add(Calendar.DAY_OF_YEAR, -3)
        return calendar.time
    }
}