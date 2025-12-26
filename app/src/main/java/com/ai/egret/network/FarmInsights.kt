package com.ai.egret.network

import com.ai.egret.models.FarmInsightsResponse
import retrofit2.http.GET
import retrofit2.http.Query



interface FarmInsightsApi {

    @GET("farm-insights")
    suspend fun getFarmInsights(
        @Query("farm_id") farmId: Int,
        @Query("history_limit") historyLimit: Int = 5,
        @Query("refresh") refresh: Boolean = false
    ): FarmInsightsResponse
}
