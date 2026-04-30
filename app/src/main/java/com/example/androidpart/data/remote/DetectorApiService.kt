package com.example.androidpart.data.remote

import com.example.androidpart.domain.model.*
import okhttp3.ResponseBody

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface DetectorApiService {

    @GET("admin/dictionaries")
    suspend fun getDictionaries(): Response<DictionariesResponse>

    @GET("admin/dictionaries/{dictName}/models")
    suspend fun getModels(
        @Path("dictName") dictName: String
    ): Response<List<String>>

    @GET("admin/models/{filename}")
    @Streaming
    suspend fun downloadModel(
        @Path("filename") filename: String
    ): Response<ResponseBody>
}