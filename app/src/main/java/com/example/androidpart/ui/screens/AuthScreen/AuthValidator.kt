package com.example.androidpart.ui.screens.AuthScreen

import android.util.Patterns

object AuthValidator {

    fun validateEmail(email: String): String? {

        if (email.isBlank()) {
            return "Введите email"
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Некорректный email"
        }
        if (email.length > 255) {
            return "Слишком длинный email"
        }

        return null
    }

    fun validatePassword(password: String): String? {

        if (password.length < 5) {
            return "Пароль слишком короткий"
        }

        return null
    }

    fun validateLogin(login: String): String? {

        if (login.isBlank()) {
            return "Введите логин"
        }

        if (login.length < 3) {
            return "Логин слишком короткий"
        }

        if (login.length > 100) {
            return "Логин слишком длинный"
        }

        return null
    }
}