package com.ai.egret.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalysisResponseDto(
    val advisory: CropHealthAdvisoryDto?,
    val confidence_note: ConfidenceNoteDto?,
    val detection_summary: DetectionSummaryDto?,
    val provider_meta: ProviderMetaDto?,
    val reference_images: List<ReferenceImageDto> = emptyList()
) : Parcelable

@Parcelize
data class CropHealthAdvisoryDto(
    val disease_name: String?,
    val disease_type: String?,
    val severity_assessment: String?,
    val recommended_actions: RecommendedActionsDto? = null
) : Parcelable

@Parcelize
data class RecommendedActionsDto(
    val application_guidelines: List<String> = emptyList(),
    val chemical_or_biological_control: List<String> = emptyList(),
    val fertilizer_management: List<String> = emptyList(),
    val immediate_treatment: List<String> = emptyList(),
    val preventive_care_next_stage: List<String> = emptyList()
) : Parcelable

@Parcelize
data class ConfidenceNoteDto(
    val level: String?, // "LOW", "HIGH"
    val message: String?
) : Parcelable

@Parcelize
data class DetectionSummaryDto(
    val crop_detected: String?,
    val crop_probability: Double?,
    val disease_detected: String?,
    val disease_probability: Double?, // e.g. 0.5
    val is_plant: Boolean = false,
    val note: String?,
    val plant_probability: Double?,
    val scientific_name: String?
) : Parcelable

@Parcelize
data class ProviderMetaDto(
    val provider: String?,
    val model_version: String?,
    val status: String?,
    val created: Double?,
    val completed: Double?
) : Parcelable

@Parcelize
data class ReferenceImageDto(
    val disease_name: String?,
    val scientific_name: String?,
    val image_url: String,
    val thumbnail_url: String?,
    val license_name: String?,
    val license_url: String?,
    val citation: String?,
    val similarity: Double?
) : Parcelable