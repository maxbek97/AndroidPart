package com.example.androidpart.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelData(
    val src: String,
    val start_position: List<Float> = listOf(0f, 0f, 0f),
    val rotation: List<Float> = listOf(0f, 0f, 0f),
    val scale: List<Float> = listOf(1f, 1f, 1f)
)
