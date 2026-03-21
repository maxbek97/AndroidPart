package com.example.androidpart.ui.screens.AuthScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpart.data.remote.AuthRepository
import com.example.androidpart.data.remote.SessionManager
import com.example.androidpart.domain.model.LoginRequest
import com.example.androidpart.domain.model.RegisterRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    fun checkAuthorization() {
        if (sessionManager.isAuthorized()) {
            viewModelScope.launch {
                _events.emit(AuthEvent.NavigateToMenu)
            }
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
                .onFailure { e ->
                    handleError(e)
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
                _events.emit(AuthEvent.NavigateToMenu)
                return@launch
            }
            _uiState.value = AuthUiState.Loading

            authRepository.login(LoginRequest(email, password))
                .onSuccess { response ->

                    // сохраняем accessToken
                    sessionManager.saveToken(response.accessToken)
                    _events.emit(AuthEvent.NavigateToMenu)
                }
                .onFailure { e ->
                    handleError(e)
                }
        }
    }
    // ===== ERROR HANDLER ===== Сюда нужно добавить обработки ошибок,
    // чтоб нормальный сообщения вызывались
    private suspend fun handleError(e: Throwable) {
        val message = e.message ?: "Ошибка"

        _uiState.value = AuthUiState.Idle

        if (
            message.contains("failed to connect", true) ||
            message.contains("Unable to resolve host", true) ||
            message.contains("Connection reset", true)
        ) {
            _events.emit(AuthEvent.NavigateToError("Сервер недоступен"))
        } else {
            _uiState.value = AuthUiState.Error(message)
        }
    }
}