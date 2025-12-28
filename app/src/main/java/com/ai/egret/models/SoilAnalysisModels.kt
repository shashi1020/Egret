package com.ai.egret.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SoilAnalysisResponseDto(
    val soil_analysis: List<SoilParameterDto> = emptyList(),
    val advisory: SoilAdvisoryDto,
    val confidence: String?,
    val meta: SoilMetaDto?
) : Parcelable

@Parcelize
data class SoilParameterDto(
    val name: String,
    val value: Double?,
    val unit: String?,
    val status: String?,
    val notes: String?
) : Parcelable

@Parcelize
data class SoilAdvisoryDto(
    val summary: String,
    val recommendations: List<SoilRecommendationDto> = emptyList()
) : Parcelable

@Parcelize
data class SoilRecommendationDto(
    val issue: String,
    val action: String,
    val priority: String
) : Parcelable

@Parcelize
data class SoilMetaDto(
    val report_id: Int,
    val created_at: String
) : Parcelable
