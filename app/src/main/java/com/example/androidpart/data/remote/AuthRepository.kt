package com.example.androidpart.data.remote
import com.example.androidpart.domain.model.*

class AuthRepository(
    private val api: AuthApiService
) {

    companion object {
        fun create(sessionManager: SessionManager): AuthRepository {
            val apiService = AuthInterceptor.AuthRetrofitClient.getApiService(sessionManager)
            return AuthRepository(apiService)
        }
    }
    // Регистрация: возвращает Result<String> (сообщение об успехе/ошибке)
    suspend fun register(dto: RegisterRequest): Result<String> =
        try {
            val response = api.register(dto)

            if (response.isSuccessful && response.body() != null) {
                // Успех
                Result.success(response.body()!!.message)
            } else {
                // Ошибка бэкенда
                Result.failure(Exception(response.errorBody()?.string() ?: "Ошибка регистрации"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    // Вход: возвращает Result<LoginResponse>
    suspend fun login(dto: LoginRequest): Result<LoginResponse> =
        try {
            val response = api.login(dto)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Ошибка входа"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}