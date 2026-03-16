package com.example.androidpart.ui.screens.AuthScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpart.data.remote.AuthRepository
import com.example.androidpart.data.remote.SessionManager
import com.example.androidpart.domain.model.LoginRequest
import com.example.androidpart.domain.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(
        login: String,
        password: String,
        email: String
    ) {
        viewModelScope.launch {

            _uiState.value = AuthUiState.Loading

            val dto = RegisterRequest(
                userLogin = login,
                password = password,
                userEmail = email
            )

            authRepository.register(dto)
                .onSuccess {
                    _uiState.value = AuthUiState.RegistrationSuccess
                }
                .onFailure {
                    _uiState.value =
                        AuthUiState.Error(it.message ?: "Ошибка регистрации")
                }
        }
    }

    fun login(
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            _uiState.value = AuthUiState.Loading

            authRepository.login(LoginRequest(email, password))
                .onSuccess { response ->

                    // сохраняем accessToken
                    SessionManager.saveToken(response.accessToken)

                    _uiState.value = AuthUiState.LoginSuccess
                }
                .onFailure {

                    _uiState.value =
                        AuthUiState.Error(it.message ?: "Ошибка входа")
                }
        }
    }
}