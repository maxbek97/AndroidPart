package com.example.androidpart.ui.screens.AuthScreen

sealed class AuthUiState {
    object Idle : AuthUiState() // Начальное состояние, бездействует
    object Loading : AuthUiState() // Идет сетевой запрос
    object RegistrationSuccess : AuthUiState() // Успешная регистрация
    object LoginSuccess : AuthUiState() // Успешный вход + роль
    data class Error(val message: String) : AuthUiState() // Ошибка (сеть, бэкенд и т.д.)
}