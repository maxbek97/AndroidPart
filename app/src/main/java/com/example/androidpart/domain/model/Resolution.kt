package com.example.androidpart.domain.model


data class Resolution(
    val width: Int,
    val height: Int,
    val scale: String
) {
    override fun toString(): String = "${width}x$height"
    fun toDisplayString(): String = "${width}x$height ($scale)"
}