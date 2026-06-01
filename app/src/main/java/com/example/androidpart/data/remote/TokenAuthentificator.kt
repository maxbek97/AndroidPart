package com.example.androidpart.data.remote

import android.util.Log
import com.example.androidpart.domain.model.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Route

class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository
) : Authenticator {

    override fun authenticate(
        route: Route?,
        response: okhttp3.Response
    ): Request? {

        Log.d("AUTH", "Получен 401, запускаем refresh")

        // защита от бесконечного цикла
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken =
            sessionManager.getRefreshToken()
                ?: return null

        val refreshResult = runBlocking {
            Log.d(
                "AUTH",
                "Refresh token найден: ${refreshToken.take(10)}..."
            )
            authRepository.refresh(
                RefreshRequest(refreshToken)
            )
        }

        return refreshResult.fold(

            onSuccess = { refreshResponse ->

                Log.d("AUTH", "Refresh успешен")
                sessionManager.saveAccessToken(
                    refreshResponse.accessToken
                )
                Log.d("AUTH", "Новый access token сохранен")
                Log.d(
                    "AUTH",
                    "Повторяем исходный запрос с новым токеном"
                )
                response.request.newBuilder()
                    .header(
                        "Authorization",
                        "Bearer ${refreshResponse.accessToken}"
                    )
                    .build()


            },

            onFailure = {
                Log.d("AUTH", "Refresh не удался")
                sessionManager.clearSession()

                null
            }
        )
    }

    private fun responseCount(
        response: okhttp3.Response
    ): Int {

        var count = 1
        var current = response.priorResponse

        while (current != null) {
            count++
            current = current.priorResponse
        }

        return count
    }
}