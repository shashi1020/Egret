package com.ai.egret.models



data class UserSoilHistoryDto(
    val history_id: Int,
    val report_id: Int,
    val farm_id: Int,
    val summary: String,
    val confidence: String,
    val created_at: String
)

