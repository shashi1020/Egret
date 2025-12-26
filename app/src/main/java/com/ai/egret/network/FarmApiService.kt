package com.ai.egret.network



import com.ai.egret.models.FarmDto
import retrofit2.http.GET
import retrofit2.http.Header

interface FarmApiService {

    @GET("farms")
    suspend fun getMyFarms(
        @Header("X-DEV-UID") devUid: String
    ): List<FarmDto>
}
