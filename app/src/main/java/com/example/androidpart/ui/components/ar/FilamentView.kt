package com.example.androidpart.ui.components.ar

import android.util.Log
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidpart.data.local.ModelManager
import com.example.androidpart.domain.ar.PoseMapper
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerPayload
import com.example.androidpart.rendering.filament.FilamentEngine
import java.io.File

@Composable
fun FilamentView(
    modifier: Modifier = Modifier,
    markers: List<ArMarker>,
) {
    val context = LocalContext.current
    val engine = remember { FilamentEngine(context) }

    // Кадр за кадром обновляем позиции моделей
    LaunchedEffect(markers) {
        markers.forEach { marker ->
            val payload = marker.payload
            if (payload is MarkerPayload.Model && marker.rvec != null && marker.tvec != null) {
                val modelName = payload.value.src
                val transform = PoseMapper.toFilamentMatrix(marker.rvec, marker.tvec)

                // Формируем путь к файлу напрямую
                val modelFile = File(context.filesDir, "models/$modelName")

                if (modelFile.exists()) {
                    engine.updateModel(
                        modelName = modelName,
                        matrix = transform,
                        modelFile = modelFile
                    )
                } else {
                    Log.e("AR_VISUAL", "Файл модели не найден: ${modelFile.absolutePath}")
                }
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply { engine.setup(this) }
        },
        modifier = modifier.fillMaxSize()
    )
}