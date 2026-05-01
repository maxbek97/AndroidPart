package com.example.androidpart.domain.model

import kotlinx.serialization.Serializable

// Ответ от сервера
@Serializable
data class MarkerResponse(
    val frame_id: Int,
    val markers: List<ArMarker>
)
