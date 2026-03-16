package com.example.androidpart.data.remote


// ВАЖНО: Убедитесь, что у вас есть импорты для Retrofit и Gson
import com.example.androidpart.BuildConfig // Импорт класса, который генерирует Gradle
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Создаем экземпляр Retrofit один раз (ленивая инициализация)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            // Используем константу, которую вы определили в build.gradle
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Предоставляем API Service для использования в Repository
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

}