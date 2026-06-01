package com.example.androidpart.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import com.example.androidpart.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor

object DetectorRetrofitClient {


    fun create(context: Context): DetectorApiService {

        val sessionManager = SessionManager(context)
        val authRepository =
            AuthRepository.create(sessionManager)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }


        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .authenticator(
                TokenAuthenticator(
                    sessionManager,
                    authRepository
                )
            )
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.DETECTOR_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DetectorApiService::class.java)
    }
}