package com.example.androidpart.domain.model

data class LoginResponse(
    val success: Boolean,
    val accessToken: String,
    val refreshToken: String
)