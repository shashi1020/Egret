package com.ai.egret.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.data.repository.FarmRepository
import com.ai.egret.models.FarmDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State is great, keep it!
data class MyFarmsUiState(
    val isLoading: Boolean = false,
    val farms: List<FarmDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel // <--- 1. Hilt Annotation
class MyFarmsViewModel @Inject constructor( // <--- 2. Inject Constructor
    private val repo: FarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyFarmsUiState())
    val uiState: StateFlow<MyFarmsUiState> = _uiState.asStateFlow()

    init {
        loadFarms()
    }

    fun loadFarms() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val farms = repo.getMyFarms()
                _uiState.value = MyFarmsUiState(
                    isLoading = false,
                    farms = farms,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = MyFarmsUiState(
                    isLoading = false,
                    farms = emptyList(),
                    error = e.message ?: "Failed to fetch farms"
                )
            }
        }
    }

    // Factory is DELETED
}