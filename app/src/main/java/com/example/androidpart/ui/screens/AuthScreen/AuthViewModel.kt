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
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun checkAuthorization() {
        if (sessionManager.isAuthorized()) {
            _uiState.value = AuthUiState.LoginSuccess
        } else {
            _uiState.value = AuthUiState.Idle
        }
    }

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
            if (email == "test@local" && password == "12345") {
                // Сразу считаем пользователя авторизованным
                sessionManager.saveToken("FAKE_TOKEN_FOR_TESTING")
                _uiState.value = AuthUiState.LoginSuccess
                return@launch
            }
            _uiState.value = AuthUiState.Loading

            authRepository.login(LoginRequest(email, password))
                .onSuccess { response ->

                    // сохраняем accessToken
                    sessionManager.saveToken(response.accessToken)

                    _uiState.value = AuthUiState.LoginSuccess
                }
                .onFailure {

                    _uiState.value =
                        AuthUiState.Error(it.message ?: "Ошибка входа")
                }
        }
    }
}