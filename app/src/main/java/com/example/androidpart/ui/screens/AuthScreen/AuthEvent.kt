package com.example.androidpart.ui.screens.AuthScreen


sealed class AuthEvent {

    object NavigateToMenu : AuthEvent()

    data class NavigateToError(val message: String) : AuthEvent()
    data class ShowMessage(
        val message: String,
        val isError: Boolean = false
    ) : AuthEvent()
}