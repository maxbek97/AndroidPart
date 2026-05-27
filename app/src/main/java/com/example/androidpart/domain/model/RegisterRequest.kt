package com.example.androidpart.domain.model

data class RegisterRequest(
    val userLogin: String,
    val password: String,
    val userEmail: String
)