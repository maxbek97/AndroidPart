package com.example.androidpart.domain.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
// Discriminator "type" говорит библиотеке смотреть на это поле для выбора класса
@JsonClassDiscriminator("type")
sealed class MarkerPayload {
    abstract val type: String

    @Serializable
    @SerialName("text")
    data class Text(
        override val type: String = "text",
        val value: TextData
    ) : MarkerPayload()

    @Serializable
    @SerialName("model")
    data class Model(
        override val type: String = "model",
        val value: ModelData
    ) : MarkerPayload()
}