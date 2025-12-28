package com.ai.egret.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.models.SoilAnalysisResponseDto
import com.ai.egret.repository.SoilAnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoilReportViewModel @Inject constructor(
    private val repo: SoilAnalysisRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: Int =
        savedStateHandle["reportId"]
            ?: error("reportId missing")

    private val _result = MutableStateFlow<SoilAnalysisResponseDto?>(null)
    val result: StateFlow<SoilAnalysisResponseDto?> = _result

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchReport()
    }

    private fun fetchReport() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _result.value = repo.getReport(reportId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load soil report"
            } finally {
                _loading.value = false
            }
        }
    }
}
