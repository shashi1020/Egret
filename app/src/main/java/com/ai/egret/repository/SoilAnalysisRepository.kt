package com.ai.egret.repository

import android.content.Context
import android.net.Uri
import com.ai.egret.models.SoilAnalysisResponseDto
import com.ai.egret.models.UserSoilHistoryDto
import com.ai.egret.network.SoilAnalysisApiService
import com.ai.egret.utils.uriToMultipart
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class SoilAnalysisRepository @Inject constructor(
    private val api: SoilAnalysisApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun analyzeSoil(uri: Uri, farmId: Int): SoilAnalysisResponseDto {
        val file = uriToMultipart(context, uri, "file")
        return api.analyzeSoil(file, farmId)
    }

    suspend fun getHistory(): List<UserSoilHistoryDto> {
        return api.getSoilHistory()
    }

    suspend fun getReport(reportId: Int): SoilAnalysisResponseDto {
        return api.getSoilReport(reportId)
    }
}
