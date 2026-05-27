package com.example.androidpart.ui.screens.AuthScreen

sealed class AuthUiState {
    object Idle : AuthUiState() // Начальное состояние, бездействует
    object Loading : AuthUiState() // Идет сетевой запрос
}