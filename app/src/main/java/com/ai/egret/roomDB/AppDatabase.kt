package com.ai.egret.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ai.egret.models.MarketPriceEntity

@Database(entities = [MarketPriceEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun marketPriceDao(): MarketPriceDao
}