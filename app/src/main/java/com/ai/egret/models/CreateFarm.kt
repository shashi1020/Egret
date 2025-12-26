package com.ai.egret.models

data class CreateFarmRequest(
    val name: String,
    val geojson: Map<String, @JvmSuppressWildcards Any>,
    val meta: Map<String, @JvmSuppressWildcards Any>? = null
)

data class CreateFarmResponse(
    val id: Int
)