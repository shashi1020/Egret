package com.ai.egret.repository

import android.content.Context
import android.net.Uri
import com.ai.egret.models.AnalysisResponseDto
import com.ai.egret.models.UserDiseaseHistoryDto
import com.ai.egret.network.CropHealthApiService
import com.ai.egret.utils.uriListToMultipartParts
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class CropHealthRepository @Inject constructor(
    private val apiService: CropHealthApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun analyzeCrop(
        imageUris: List<Uri>,
        farmId: Int
    ): AnalysisResponseDto {
        val parts = uriListToMultipartParts(context, imageUris)
        return apiService.analyzeCrop(parts, farmId)
    }


    suspend fun getUserDiseaseHistory(): List<UserDiseaseHistoryDto> {
        return apiService.getUserDiseaseHistory()
    }

    suspend fun getAnalysisById(id: Int): AnalysisResponseDto {
        return apiService.getCropHealthAnalysis(id)
    }

}
