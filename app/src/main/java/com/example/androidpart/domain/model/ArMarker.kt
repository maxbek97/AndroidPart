package com.example.androidpart.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ArMarker(
    val id: Int,
    // Углы приходят как список из 4 точек, где каждая точка — список [x, y]
    val corners: List<List<List<Float>>>,
    val rvec: List<Double>? = null,
    val tvec: List<Double>? = null,
    val payload: MarkerPayload? = null
)