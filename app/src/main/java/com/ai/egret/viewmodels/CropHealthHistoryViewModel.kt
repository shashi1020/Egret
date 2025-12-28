package com.ai.egret.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.models.UserDiseaseHistoryDto
import com.ai.egret.repository.CropHealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropHealthHistoryViewModel @Inject constructor(
    private val repo: CropHealthRepository
) : ViewModel() {

    private val _history = MutableStateFlow<List<UserDiseaseHistoryDto>>(emptyList())
    val history: StateFlow<List<UserDiseaseHistoryDto>> = _history

    fun load() {
        viewModelScope.launch {
            _history.value = repo.getUserDiseaseHistory()
        }
    }
}

