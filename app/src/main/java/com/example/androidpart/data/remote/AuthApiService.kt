package com.example.androidpart.data.remote

import com.example.androidpart.domain.model.*

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(
        @Body body: RefreshRequest
    ): Response<RefreshResponse>

}