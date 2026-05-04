package com.example.androidpart.ui.components.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.example.androidpart.domain.model.Resolution

// CameraUtils.kt
object CameraUtils {
    fun getFilteredResolutions(context: Context): List<Resolution> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val result = mutableListOf<Resolution>()

        try {
            val cameraId = cameraManager.cameraIdList.getOrNull(0) ?: return emptyList()
            val chars = cameraManager.getCameraCharacteristics(cameraId)
            val map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = map?.getOutputSizes(ImageFormat.YUV_420_888) ?: return emptyList()

            // Настраиваем фильтры
            val maxPixels = 1088 * 1088 // Ограничение Full HD
            val minPixels = 320 * 240  // Отсекаем совсем мелкие

            sizes.forEach { size ->
                val pixels = size.width * size.height
                val aspect = size.width.toFloat() / size.height.toFloat()

                // Определяем соотношение с небольшой погрешностью
                val label = when {
                    aspect in 0.95f..1.05f -> "1:1"
                    aspect in 1.32f..1.35f -> "4:3"
                    else -> null // Игнорируем остальные (например, 21:9)
                }

                if (label != null && pixels in minPixels..maxPixels) {
                    result.add(Resolution(size.width, size.height, label))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Сортируем: сначала по соотношению сторон, потом по размеру
        return result.sortedBy { it.width * it.height }
    }
}