package com.ai.egret.models


import com.google.gson.annotations.SerializedName

// Matches backend /farms response shape
data class FarmDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,

    @SerializedName("meta") val meta: Map<String, @JvmSuppressWildcards Any>?
)
