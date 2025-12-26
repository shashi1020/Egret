package com.ai.egret.data.repository

import android.util.Log
import com.ai.egret.models.CreateFarmRequest
import com.ai.egret.models.FarmDto
import com.ai.egret.network.ApiService
import com.ai.egret.network.FarmApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FarmRepository @Inject constructor(
    private val apiService: ApiService,       // For createFarm (General API)
    private val farmApi: FarmApiService,      // For getMyFarms (Farm Specific API)
    private val auth: FirebaseAuth
) {

    // --- 1. Get My Farms (The new function you just pasted) ---
    suspend fun getMyFarms(): List<FarmDto> {
        Log.d("FarmRepository", "Requesting farms from server...")

        val user = auth.currentUser
        if (user == null) {
            Log.e("FarmRepository", "No Firebase user. User must log in first.")
            throw IllegalStateException("User not logged in")
        }

        val uid = user.uid
        Log.d("FarmRepository", "Calling /farms with X-DEV-UID=$uid")

        // Using the injected API
        val farms = farmApi.getMyFarms(devUid = uid)
        Log.d("FarmRepository", "Got ${farms.size} farms")
        return farms
    }

    // --- 2. Create Farm (From the previous step) ---
    suspend fun createFarmOnServer(
        name: String,
        geojsonMap: Map<String, Any>,
        meta: Map<String, Any>? = null
    ): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

                val req = CreateFarmRequest(name = name, geojson = geojsonMap, meta = meta)
                val resp = apiService.createFarm("Bearer $token", req)

                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null) {
                        Result.success(body.id)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    val err = try { resp.errorBody()?.string() } catch (e: Exception) { resp.code().toString() }
                    Result.failure(Exception("HTTP ${resp.code()}: $err"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // --- Helper ---
    private suspend fun getAuthToken(): String? {
        val user = auth.currentUser ?: return null
        return try {
            val result = user.getIdToken(true).await()
            result.token
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}