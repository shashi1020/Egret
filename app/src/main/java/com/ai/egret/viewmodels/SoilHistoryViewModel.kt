package com.ai.egret.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.models.UserSoilHistoryDto
import com.ai.egret.repository.SoilAnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoilHistoryViewModel @Inject constructor(
    private val repo: SoilAnalysisRepository
) : ViewModel() {

    private val _history = MutableStateFlow<List<UserSoilHistoryDto>>(emptyList())
    val history: StateFlow<List<UserSoilHistoryDto>> = _history

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _history.value = repo.getHistory()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load soil history"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
