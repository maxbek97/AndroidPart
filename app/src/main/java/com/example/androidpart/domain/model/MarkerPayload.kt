package com.example.androidpart.domain.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MarkerPayload.MarkerPayloadSerializer::class)
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

    @Serializable
    data object None : MarkerPayload() {
        override val type: String = "none"
    }

    object MarkerPayloadSerializer : JsonContentPolymorphicSerializer<MarkerPayload>(MarkerPayload::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<MarkerPayload> {
            val json = element.jsonObject
            return when (json["type"]?.jsonPrimitive?.content) {
                "text" -> MarkerPayload.Text.serializer()
                "model" -> MarkerPayload.Model.serializer()
                else -> MarkerPayload.None.serializer()
            }
        }
    }
}