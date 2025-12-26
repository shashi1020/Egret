package com.ai.egret.models


/* ---------------- TOP LEVEL ---------------- */

data class FarmInsightsResponse(
    val farm: FarmInsightsDTO,
    val advisory: AdvisoryDto,
    val history: List<HistoryItemDto>
)

/* ---------------- FARM ---------------- */

data class FarmInsightsDTO(
    val id: Int,
    val name: String?,
    val area_m2: Double?,
    val created_at: String?,
    val crop: String?,                 // "rice", "sugarcane"
    val meta: FarmMetaDto?
)

data class FarmMetaDto(
    val crop_id: Int?,
    val crop_name: String?,
    val created_from: String?
)

/* ---------------- ADVISORY ---------------- */

data class AdvisoryDto(
    val farm_id: Int,

    val severity: String,              // INFO | WARNING | CRITICAL
    val stress_score: Double,

    val confidence: String,            // LOW | MEDIUM | HIGH
    val confidence_score: Double,

    val crop_type: String?,
    val crop_stage: String?,

    val signals: SignalsDto,
    val explanation: ExplanationDto,
    val recommended_actions: List<String>,

    val meta: AdvisoryMetaDto,

    val generated_at: String,
    val scene_datetime: String?
)

/* ---------------- SIGNALS (for indicators / pie) ---------------- */

data class SignalsDto(
    val ndvi_variance: Double?,
    val ndvi_drop: Double?,
    val prop_low: Double?,
    val temporal_slope: Double?,
    val health_drop: Double?,
    val recency: Double?,
    val weather: Double?
)

/* ---------------- META (CURRENT STATE LIVES HERE) ---------------- */

data class AdvisoryMetaDto(
    val cur_ndvi: Double?,
    val baseline_ndvi: Double?,
    val ndvi_drop_abs: Double?,

    val spatial: SpatialDto?,
    val temporal: TemporalDto?,

    val latest_pr: LatestProcessResultDto
)

/* ---------------- PROCESS RESULT (SATELLITE SNAPSHOT) ---------------- */

data class LatestProcessResultDto(
    val scene_id: String?,
    val health_score: Double?,
    val format_used: String?,
    val indices_stats: IndicesStatsDto?,
    val previews_base64: PreviewImagesDto?,
    val provenance: ProvenanceDto?
)

data class IndicesStatsDto(
    val ndvi: NdviStatsDto?
)

data class NdviStatsDto(
    val min: Double?,
    val max: Double?,
    val mean: Double?,
    val median: Double?,
    val p10: Double?,
    val p90: Double?,
    val std: Double?
)

data class PreviewImagesDto(
    val ndvi: String?    // base64 image
)

data class ProvenanceDto(
    val collection: String?,
    val scene_id: String?,
    val used_start: String?,
    val used_end: String?,
    val note: String?
)

/* ---------------- SPATIAL & TEMPORAL ---------------- */

data class SpatialDto(
    val mean: Double?,
    val p10: Double?,
    val p90: Double?,
    val spread: Double?,
    val std: Double?,
    val variance_norm: Double?,
    val prop_low_estimate: Double?
)

data class TemporalDto(
    val history_count: Int?,
    val recency_days: Int?,
    val recency_score: Double?,
    val slope_norm: Double?,
    val slope_per_day: Double?
)

/* ---------------- EXPLANATION ---------------- */

data class ExplanationDto(
    val causes: List<String>,
    val triggers: List<String>,
    val top_contributors: List<ContributorDto>,
    val confidence_rationale: ConfidenceRationaleDto
)

data class ContributorDto(
    val name: String,
    val value: Double,
    val contribution: Double
)

data class ConfidenceRationaleDto(
    val coverage: Double,
    val data_quality: String,
    val temporal_recency_days: Int
)

/* ---------------- HISTORY (PAST PASSES) ---------------- */

data class HistoryItemDto(
    val scene_id: String?,
    val timestamp: String?,
    val health_score: Double?,
    val ndvi_mean: Double?
)
