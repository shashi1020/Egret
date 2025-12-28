package com.ai.egret.network

import com.ai.egret.models.SoilAnalysisResponseDto
import com.ai.egret.models.UserSoilHistoryDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SoilAnalysisApiService {

    @Multipart
    @POST("soil-analysis")
    suspend fun analyzeSoil(
        @Part file: MultipartBody.Part,
        @Part("farm_id") farmId: Int
    ): SoilAnalysisResponseDto

    @GET("soil-analysis/history")
    suspend fun getSoilHistory(): List<UserSoilHistoryDto>

    @GET("soil-analysis/report/{reportId}")
    suspend fun getSoilReport(
        @Path("reportId") reportId: Int
    ): SoilAnalysisResponseDto
}
