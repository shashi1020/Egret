package com.ai.egret.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.data.repository.FarmRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

@HiltViewModel
class FieldRegistrationViewModel @Inject constructor(
    private val repo: FarmRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun registerFarm(name: String, crop: String, points: List<LatLng>) {
        if (name.isBlank() || crop.isBlank()) {
            _state.value = RegistrationState.Error("Please fill all fields")
            return
        }
        if (points.size < 3) {
            _state.value = RegistrationState.Error("At least 3 points required")
            return
        }

        viewModelScope.launch {
            _state.value = RegistrationState.Loading
            try {
                // 1. Prepare GeoJSON
                val geoJsonMap = createGeoJsonMap(points)

                // 2. Prepare Meta Data
                val meta = mapOf(
                    "crop_name" to crop.trim(),
                    "created_from" to "android_app"
                )

                // 3. Call Repository
                val result = repo.createFarmOnServer(
                    name = name.trim(),
                    geojsonMap = geoJsonMap,
                    meta = meta
                )

                result.onSuccess {
                    _state.value = RegistrationState.Success
                }.onFailure { e ->
                    _state.value = RegistrationState.Error(e.message ?: "Failed to save farm")
                }
            } catch (e: Exception) {
                _state.value = RegistrationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _state.value = RegistrationState.Idle
    }

    // Helper: Converts LatLng list to the Map structure your API expects
    private fun createGeoJsonMap(points: List<LatLng>): Map<String, Any> {
        // Ensure the polygon is closed (first point == last point)
        val closedPoints = if (points.first() == points.last()) points else points + points.first()

        // GeoJSON expects [Longitude, Latitude] order
        val coordinates = closedPoints.map { listOf(it.longitude, it.latitude) }

        return mapOf(
            "type" to "Polygon",
            "coordinates" to listOf(coordinates) // Polygon needs a list of rings (we only have 1 outer ring)
        )
    }
}