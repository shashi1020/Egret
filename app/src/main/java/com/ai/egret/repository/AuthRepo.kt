package com.ai.egret.repository


import android.util.Log
import com.ai.egret.network.ApiService
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val apiService: ApiService
) {

    // --- 1. Generic Firebase Sign In (Used for Email, Phone, Google) ---
    suspend fun signInWithCredential(credential: AuthCredential): Result<Boolean> {
        return try {
            auth.signInWithCredential(credential).await()
            // After Firebase login, sync with your backend
            syncUserWithBackend()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 2. Backend Sync (The "getMe" call) ---
    private suspend fun syncUserWithBackend() {
        val user = auth.currentUser ?: return
        try {
            val tokenResult = user.getIdToken(true).await()
            val token = tokenResult.token
            if (token != null) {
                // We don't return the result, just ensure it executes
                val response = apiService.getMe("Bearer $token")
                if (!response.isSuccessful) {
                    Log.e("AuthRepo", "Backend Sync Failed: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Backend Sync Error", e)
            // We suppress error here so the user can still login to Firebase
            // even if your backend is momentarily down.
        }
    }

    // --- 3. Email Specifics ---
    suspend fun signInEmail(email: String, pass: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            syncUserWithBackend()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpEmail(email: String, pass: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            syncUserWithBackend()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}