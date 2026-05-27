package com.example.androidpart.domain.model

import com.google.gson.annotations.SerializedName

data class DictionariesResponse(
    @SerializedName("current_dict")
    val currentDict: String,

    @SerializedName("dict_names")
    val dictNames: List<String>,

    @SerializedName("models_hash")
    val modelsHash: String
)