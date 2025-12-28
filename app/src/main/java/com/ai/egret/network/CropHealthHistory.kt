package com.ai.egret.network

import com.ai.egret.models.AnalysisResponseDto
import com.ai.egret.models.UserDiseaseHistoryDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CropHealthApiService {
    @Multipart
    @POST("crop-health")
    suspend fun analyzeCrop(
        @Part files: List<MultipartBody.Part>,
        @Part("farm_id") farmId: Int
    ): AnalysisResponseDto

    @GET("crop-health/history")
    suspend fun getUserDiseaseHistory(): List<UserDiseaseHistoryDto>

    @GET("crop-health/analysis/{analysisId}")
    suspend fun getCropHealthAnalysis(
        @Path("analysisId") analysisId: Int
    ): AnalysisResponseDto
}
