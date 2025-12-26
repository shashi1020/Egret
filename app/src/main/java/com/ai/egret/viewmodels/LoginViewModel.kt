package com.ai.egret.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.egret.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 1. Email Login
    fun loginWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            repo.signInEmail(email, pass)
                .onSuccess { _uiState.value = LoginUiState.Success }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Login failed") }
        }
    }

    // 2. Email Sign Up
    fun createAccount(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            repo.signUpEmail(email, pass)
                .onSuccess { _uiState.value = LoginUiState.Success }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Signup failed") }
        }
    }

    // 3. General Credential Sign In (Google / Phone)
    fun signInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            repo.signInWithCredential(credential)
                .onSuccess { _uiState.value = LoginUiState.Success }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Auth failed") }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}