package com.example.androidpart.domain.model

data class LoginRequest(
    val userEmail: String, // Название поля должно соответствовать полю в LoginDTO
    val password: String
)