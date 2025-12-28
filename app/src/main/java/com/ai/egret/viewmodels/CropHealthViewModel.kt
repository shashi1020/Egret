package com.ai.egret.viewmodels

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.models.AnalysisResponseDto
import com.ai.egret.repository.CropHealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropHealthViewModel @Inject constructor(
    private val repo: CropHealthRepository,
    savedStateHandle: SavedStateHandle   // ✅ REQUIRED
) : ViewModel() {

    private val farmId: Int =
        savedStateHandle.get<Int>("farmId")
            ?: error("farmId missing in CropHealthViewModel")


    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _analysisResult = MutableStateFlow<AnalysisResponseDto?>(null)
    val analysisResult: StateFlow<AnalysisResponseDto?> = _analysisResult

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun setSelectedImages(uris: List<Uri>) {
        _selectedImages.value = uris
    }

    fun analyze(onSuccess: (AnalysisResponseDto) -> Unit) {
        val uris = _selectedImages.value
        if (uris.isEmpty()) {
            _error.value = "Pick at least one image to analyze"
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _error.value = null
            try {
                val result = repo.analyzeCrop(
                    imageUris = uris,
                    farmId = farmId        // ✅ FIXED
                )
                _analysisResult.value = result
                onSuccess(result)
            } catch (e: Exception) {
                _error.value = e.message ?: "Analysis failed"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}
