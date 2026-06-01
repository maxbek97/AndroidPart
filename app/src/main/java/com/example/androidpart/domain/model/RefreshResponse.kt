package com.example.androidpart.domain.model

data class RefreshResponse(
    val success: Boolean,
    val accessToken: String
)

data class ErrorResponse(
    val success: Boolean,
    val message: String
)