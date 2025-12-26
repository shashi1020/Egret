package com.ai.egret.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_prices")
data class MarketPriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val state: String,
    val district: String,
    val market: String,
    val commodity: String,
    val variety: String,
    val grade: String,
    val arrivalDate: String,
    val minPrice: String,
    val maxPrice: String,
    val modalPrice: String,
    val commodityCode: String
)
