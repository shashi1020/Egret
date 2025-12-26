package com.ai.egret.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.models.DailySummary
import com.ai.egret.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Define the State (The UI listens to this)
sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val daily: List<DailySummary>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    // 2. Ideally move this to local.properties later
    private val API_KEY = "5798cb24d1b2e06fc20e672fa574705f"

    // 3. Single Source of Truth for State
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadForLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            try {
                // Fetch data
                val data = repository.fetchForecastAndAggregate(lat, lon, API_KEY)

                if (data.isNotEmpty()) {
                    _uiState.value = WeatherUiState.Success(data)
                } else {
                    _uiState.value = WeatherUiState.Error("No weather data found.")
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.message ?: "Failed to load weather")
            }
        }
    }
}