package com.example.androidpart.domain.model
import kotlinx.serialization.Serializable

@Serializable
data class FramePayload(
    val frame_id: Int,
    val image: String
)