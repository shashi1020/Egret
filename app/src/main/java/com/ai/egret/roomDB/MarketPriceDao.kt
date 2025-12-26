package com.ai.egret.roomDB

import androidx.room.*
import com.ai.egret.models.MarketPriceEntity

@Dao
interface MarketPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: MarketPriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prices: List<MarketPriceEntity>)

    @Query("SELECT * FROM market_prices")
    suspend fun getAllPrices(): List<MarketPriceEntity>


    @Query("DELETE FROM market_prices WHERE arrivalDate < :dateLimit")
    suspend fun deleteOldData(dateLimit: String)

    @Query("SELECT * FROM market_prices WHERE arrivalDate BETWEEN :startDate AND :endDate")
    suspend fun getPricesBetweenDates(startDate: String, endDate: String): List<MarketPriceEntity>

    @Query("DELETE FROM market_prices")
    suspend fun clearAllMarketPrices()


}
