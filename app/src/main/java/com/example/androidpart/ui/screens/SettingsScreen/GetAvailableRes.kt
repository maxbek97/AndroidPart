package com.example.androidpart.ui.screens.SettingsScreen

import com.example.androidpart.domain.model.Resolution

fun getAvailableRes(
    screenWidth: Int,
    screenHeight: Int
): List<Resolution> {

    val baseWidth = 1080
    val baseHeight = 1200

    val scales = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f)

    return scales.map {
        Resolution(
            (baseWidth * it).toInt(),
            (baseHeight * it).toInt()
        )
    }.filter {
        it.width <= screenWidth && it.height <= screenHeight
    }
}