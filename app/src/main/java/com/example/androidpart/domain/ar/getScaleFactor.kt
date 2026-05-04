package com.example.androidpart.domain.ar

import androidx.compose.ui.geometry.Offset

fun getScaleFactors(
    contentWidth: Float,  // Разрешение из настроек (например, 640)
    contentHeight: Float, // Разрешение из настроек (например, 480)
    viewWidth: Float,     // Реальная ширина контейнера глаза на экране
    viewHeight: Float     // Реальная высота контейнера глаза на экране
): Pair<Float, Offset> {
    val scale = maxOf(viewWidth / contentWidth, viewHeight / contentHeight)
    val offsetX = (viewWidth - contentWidth * scale) / 2f
    val offsetY = (viewHeight - contentHeight * scale) / 2f
    return Pair(scale, Offset(offsetX, offsetY))
}