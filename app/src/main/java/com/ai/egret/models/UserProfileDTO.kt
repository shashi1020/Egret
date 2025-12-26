package com.ai.egret.models


import com.google.gson.annotations.SerializedName

data class UserProfileDto(
    val id: Long,

    @SerializedName("firebase_uid")
    val uid: String,

    val email: String?,
    val phone: String?
)
