package com.ai.egret.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.data.mapper.toResponse
import com.ai.egret.models.MarketPriceResponse
import com.ai.egret.repository.MarketPriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketPriceViewModel @Inject constructor(
    private val repository: MarketPriceRepository
) : ViewModel() {

    private val _prices = MutableStateFlow<List<MarketPriceResponse>>(emptyList())
    val prices: StateFlow<List<MarketPriceResponse>> = _prices.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Load data immediately when ViewModel is created (optional, but good for UX)
    init {
        loadPricesFromDB()
    }

    // 1. Read from Local Database (Fast, works offline)
    private fun loadPricesFromDB() {
        viewModelScope.launch {
            val entities = repository.getLocalPrices()
            // Convert DB Entity -> UI Model
            _prices.value = entities.map { it.toResponse() }
        }
    }

    // 2. Fetch New Data (API -> DB -> UI)
    fun loadPrices(apiKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // A. Fetch from API and save to DB
                repository.refreshMarketPrices(apiKey)

                // B. Reload the fresh data from DB
                loadPricesFromDB()

            } catch (e: Exception) {
                _error.value = "Failed to refresh: ${e.message}"
                // Even if API fails, we still show old DB data (Offline support!)
                loadPricesFromDB()
            } finally {
                _isLoading.value = false
            }
        }
    }
}