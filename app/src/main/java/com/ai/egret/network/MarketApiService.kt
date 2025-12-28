package com.ai.egret.network


import com.ai.egret.models.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface MarketApiService {
//    35985678-0d79-46b4-9ed6-6f13308a1d24
    @GET("resource/9ef84268-d588-465a-a308-a864a43d0070")
    suspend fun getMarketPrices(
        @Query("api-key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 500,
        @Query("filters[state.keyword]") state: String? = null,
        @Query("filters[district]") district: String? = null,
        @Query("filters[commodity]") commodity: String? = null,
        @Query("filters[arrival_date]") arrivalDate: String? = null // <-- correct name, lowercase
    ): ApiResponse
}
