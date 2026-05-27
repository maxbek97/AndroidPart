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
            val loginError =
                AuthValidator.validateLogin(login)

            if (loginError != null) {
                _events.emit(
                    AuthEvent.ShowMessage(
                        loginError,
                        isError = true
                    )
                )
                return@launch
            }
            val emailError =
                AuthValidator.validateEmail(email)

            if (emailError != null) {

                _events.emit(
                    AuthEvent.ShowMessage(
                        emailError,
                        isError = true
                    )
                )

                return@launch
            }

            val passwordError =
                AuthValidator.validatePassword(password)

            if (passwordError != null) {

                _events.emit(
                    AuthEvent.ShowMessage(
                        passwordError,
                        isError = true
                    )
                )

                return@launch
            }
            _uiState.value = AuthUiState.Loading

            val dto = RegisterRequest(
                userLogin = login,
                password = password,
                userEmail = email
            )

            authRepository.register(dto)
                .onSuccess {
                    _uiState.value = AuthUiState.Idle
                    _events.emit(
                        AuthEvent.ShowMessage(
                            "Регистрация прошла успешно",
                            isError = false
                        )
                    )

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
            val emailError =
                AuthValidator.validateEmail(email)

            if (emailError != null) {

                _events.emit(
                    AuthEvent.ShowMessage(
                        emailError,
                        isError = true
                    )
                )

                return@launch
            }

            val passwordError =
                AuthValidator.validatePassword(password)

            if (passwordError != null) {

                _events.emit(
                    AuthEvent.ShowMessage(
                        passwordError,
                        isError = true
                    )
                )

                return@launch
            }

            _uiState.value = AuthUiState.Loading

            authRepository.login(LoginRequest(email, password))
                .onSuccess { response ->
                    _uiState.value = AuthUiState.Idle
                    sessionManager.saveToken(response.accessToken)
                    _events.emit(
                        AuthEvent.ShowMessage(
                            "Добро пожаловать",
                            isError = false
                        )
                    )
                    _events.emit(AuthEvent.NavigateToMenu)
                }
                .onFailure { e ->
                    handleError(e)
                }
        }
    }
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
            _events.emit(
                AuthEvent.ShowMessage(
                    message = message,
                    isError = true
                )
            )
        }
    }
}
