package com.example.androidpart.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelData(
    val src: String,
    val start_position: List<Float>
)
