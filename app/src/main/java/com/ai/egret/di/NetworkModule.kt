package com.ai.egret.di

import com.ai.egret.network.ApiService
import com.ai.egret.network.FarmApiService
import com.ai.egret.network.FarmInsightsApi
import com.ai.egret.network.MarketApiService
import com.ai.egret.network.OpenWeatherApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // --- 1. Converters ---

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideGsonFactory(): GsonConverterFactory = GsonConverterFactory.create()

    @Provides
    @Singleton
    fun provideMoshiFactory(moshi: Moshi): MoshiConverterFactory = MoshiConverterFactory.create(moshi)

    // --- 2. Retrofit Instances (The "Clients") ---

    @GovDataNetwork
    @Provides
    @Singleton
    fun provideGovRetrofit(moshiFactory: MoshiConverterFactory): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.data.gov.in/")
            .addConverterFactory(moshiFactory)
            .build()
    }

    @WeatherNetwork
    @Provides
    @Singleton
    fun provideWeatherRetrofit(moshiFactory: MoshiConverterFactory): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(moshiFactory)
            .build()
    }

    @BackendNetwork
    @Provides
    @Singleton
    fun provideBackendRetrofit(gsonFactory: GsonConverterFactory): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://laughably-unexcusable-celestina.ngrok-free.dev/") // Note: Ngrok URLs change often!
            .addConverterFactory(gsonFactory)
            .build()
    }

    // --- 3. API Services (The "Endpoints") ---

    @Provides
    @Singleton
    fun provideMarketApi(@GovDataNetwork retrofit: Retrofit): MarketApiService {
        return retrofit.create(MarketApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherApi(@WeatherNetwork retrofit: Retrofit): OpenWeatherApiService {
        return retrofit.create(OpenWeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBackendApi(@BackendNetwork retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFarmApi(@BackendNetwork retrofit: Retrofit): FarmApiService {
        return retrofit.create(FarmApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFarmInsightsApi(@BackendNetwork retrofit: Retrofit): FarmInsightsApi {
        return retrofit.create(FarmInsightsApi::class.java)
    }
}