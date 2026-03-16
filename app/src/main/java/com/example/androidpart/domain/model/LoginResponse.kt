package com.example.androidpart.domain.model

data class LoginResponse(
    val success: Boolean, // Название поля должно соответствовать полю в LoginDTO
    val accessToken: String,
    val refreshToken: String
)