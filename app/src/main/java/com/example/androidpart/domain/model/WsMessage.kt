package com.example.androidpart.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class WsMessage {
    @Serializable
    @SerialName("init")
    data class Init(
        val camera_matrix: List<List<Double>>,
        val dist_coeffs: List<Double>,
        val marker_length: Double
    ) : WsMessage()

    @Serializable
    @SerialName("frame")
    data class Frame(
        val frame_id: Int,
        val image: String
    ) : WsMessage()
}

