package com.example.androidpart.domain.model

import com.google.gson.annotations.SerializedName

data class DictionariesResponse(
    @SerializedName("current_dict")
    val currentDict: String, // Название поля должно соответствовать полю в LoginDTO

    @SerializedName("dict_names")
    val dictNames: List<String>,

    @SerializedName("models_hash")
    val modelsHash: String
)