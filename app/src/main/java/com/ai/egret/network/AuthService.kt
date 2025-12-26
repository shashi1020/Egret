package com.ai.egret.network



import com.ai.egret.models.CreateFarmRequest
import com.ai.egret.models.CreateFarmResponse
import com.ai.egret.models.UserProfileDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("/user/me")
    suspend fun getMe(@Header("Authorization") bearer: String): Response<UserProfileDto>

    @PATCH("/user/me")
    suspend fun patchMe(
        @Header("Authorization") bearer: String,
        @Body updates: Map<String, @JvmSuppressWildcards Any>
    ): Response<UserProfileDto>

    @POST("/user/signup")
    suspend fun signup(
        @Header("Authorization") bearer: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<UserProfileDto>

    @POST("/farms")
    suspend fun createFarm(
        @Header("Authorization") bearer: String,
        @Body req: CreateFarmRequest
    ): retrofit2.Response<CreateFarmResponse>

}
