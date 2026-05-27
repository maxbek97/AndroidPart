package com.example.androidpart.domain.ar

import androidx.compose.ui.geometry.Offset

fun getScaleFactors(
    contentWidth: Float,  // Разрешение из настроек
    contentHeight: Float,
    viewWidth: Float,     // Реальная ширина контейнера
    viewHeight: Float     // Реальная высота контейнера
): Pair<Float, Offset> {
    val scale = maxOf(viewWidth / contentWidth, viewHeight / contentHeight)
    val offsetX = (viewWidth - contentWidth * scale) / 2f
    val offsetY = (viewHeight - contentHeight * scale) / 2f
    return Pair(scale, Offset(offsetX, offsetY))
}