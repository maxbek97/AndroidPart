package com.example.androidpart.data.remote

import com.example.androidpart.domain.model.DictionariesResponse
import okhttp3.ResponseBody

class DetectorRepository(
    private val api: DetectorApiService
) {

    suspend fun getDictionaries(): Result<DictionariesResponse> =
        try {
            val response = api.getDictionaries()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения словарей"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getModels(dictName: String): Result<List<String>> =
        try {
            val response = api.getModels(dictName)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка получения моделей"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun downloadModel(filename: String): Result<ResponseBody> =
        try {
            val response = api.downloadModel(filename)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ошибка загрузки файла"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}