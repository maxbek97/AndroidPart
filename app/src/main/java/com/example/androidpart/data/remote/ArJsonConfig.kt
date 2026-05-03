package com.example.androidpart.data.remote

import kotlinx.serialization.json.Json

val arJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
    classDiscriminator = "type"
}