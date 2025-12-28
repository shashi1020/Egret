package com.ai.egret.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.models.AnalysisResponseDto
import com.ai.egret.repository.CropHealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CropHealthDetailViewModel @Inject constructor(
    private val repo: CropHealthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val analysisId: Int? = savedStateHandle["analysisId"]

    private val _result = MutableStateFlow<AnalysisResponseDto?>(null)
    val result: StateFlow<AnalysisResponseDto?> = _result

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        analysisId?.let { fetch(it) }
    }

    private fun fetch(id: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _result.value = repo.getAnalysisById(id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load analysis"
            } finally {
                _loading.value = false
            }
        }
    }


    /** Used by LIVE upload flow */
    fun setLocalResult(result: AnalysisResponseDto) {
        _result.value = result
        _loading.value = false
    }
}
