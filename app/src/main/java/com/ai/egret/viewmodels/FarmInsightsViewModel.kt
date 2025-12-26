package com.ai.egret.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.models.FarmInsightsResponse
import com.ai.egret.repository.FarmInsightsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ai.egret.utils.*
sealed class FarmInsightsUiState {
    object Loading : FarmInsightsUiState()
    data class Loaded(
        val data: FarmInsightsResponse,
        val historySummary: String // Pre-calculated natural language string
    ) : FarmInsightsUiState()
    data class Error(val message: String) : FarmInsightsUiState()
}

@HiltViewModel
class FarmInsightsViewModel @Inject constructor(
    private val repository: FarmInsightsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FarmInsightsUiState>(FarmInsightsUiState.Loading)
    val uiState: StateFlow<FarmInsightsUiState> = _uiState.asStateFlow()

    fun loadInsights(farmId: Int) {
        _uiState.value = FarmInsightsUiState.Loading

        viewModelScope.launch {
            try {
                val response = repository.getFarmInsights(
                    farmId = farmId,
                    historyLimit = 5
                )

                // Logic: Generate the Natural Language Summary here
                // We use the extension function we created in Extensions.kt
                // We access the history list from the response
                // Import your extension function package if needed
                val summary = response.history.toNaturalLanguageSummary()

                _uiState.value = FarmInsightsUiState.Loaded(
                    data = response,
                    historySummary = summary
                )

            } catch (e: Exception) {
                _uiState.value = FarmInsightsUiState.Error(
                    message = e.message ?: "Failed to load crop insights."
                )
            }
        }
    }
}