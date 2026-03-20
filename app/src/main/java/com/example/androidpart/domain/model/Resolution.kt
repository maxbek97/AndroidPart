package com.example.androidpart.domain.model


data class Resolution(
    val width: Int,
    val height: Int
) {
    override fun toString(): String = "${width}x$height"
}