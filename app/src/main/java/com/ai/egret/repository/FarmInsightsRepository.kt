package com.ai.egret.repository


import com.ai.egret.models.FarmInsightsResponse
import com.ai.egret.network.FarmInsightsApi
import javax.inject.Inject

// 1. The Interface (Contract)
interface FarmInsightsRepository {
    suspend fun getFarmInsights(
        farmId: Int,
        historyLimit: Int = 5,
        refresh: Boolean = false
    ): FarmInsightsResponse
}

// 2. The Implementation (Logic)
class FarmInsightsRepositoryImpl @Inject constructor(
    private val api: FarmInsightsApi
) : FarmInsightsRepository {

    override suspend fun getFarmInsights(
        farmId: Int,
        historyLimit: Int,
        refresh: Boolean
    ): FarmInsightsResponse {
        return api.getFarmInsights(
            farmId = farmId,
            historyLimit = historyLimit,
            refresh = refresh
        )
    }
}