package com.ai.egret.di


import android.content.Context
import androidx.room.Room
import com.ai.egret.roomDB.AppDatabase
import com.ai.egret.roomDB.MarketPriceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "market_price_db"
        )
            // .fallbackToDestructiveMigration() // Uncomment if you want to wipe data on schema change
            .build()
    }

    @Provides
    fun provideMarketPriceDao(database: AppDatabase): MarketPriceDao {
        return database.marketPriceDao()
    }
}