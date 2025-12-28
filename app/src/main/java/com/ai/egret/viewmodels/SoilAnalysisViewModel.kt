package com.ai.egret.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.models.SoilAnalysisResponseDto
import com.ai.egret.repository.SoilAnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoilAnalysisViewModel @Inject constructor(
    private val repo: SoilAnalysisRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- FIX 2: Handle missing farmId gracefully (Use default or 0 for now) ---
    // In production, your route should be "soil_report/{farmId}"
    private val farmId: Int = savedStateHandle["farmId"] ?: 3

    private val _result = MutableStateFlow<SoilAnalysisResponseDto?>(null)
    val result: StateFlow<SoilAnalysisResponseDto?> = _result

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun analyze(uri: Uri) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _result.value = null // Clear old result before starting
            try {
                // The repo call handles the heavy lifting
                _result.value = repo.analyzeSoil(uri, farmId)
            } catch (e: Exception) {
                _error.value = "Analysis failed: ${e.message}"
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    // --- FIX 3: Call this AFTER navigating to prevent loops ---
    fun onResultConsumed() {
        _result.value = null
    }
}