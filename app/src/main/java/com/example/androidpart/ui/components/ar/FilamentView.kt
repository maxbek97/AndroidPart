package com.example.androidpart.ui.components.ar

import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.CameraIntrinsics
import com.example.androidpart.domain.model.MarkerPayload
import com.example.androidpart.domain.ar.Eye
import com.example.androidpart.domain.ar.FilamentEngine
import java.io.File

@Composable
fun FilamentView(
    modifier: Modifier = Modifier,
    markers: List<ArMarker>,
    engine: FilamentEngine,
    eye: Eye,
    frameWidth: Float,
    frameHeight: Float,
    calibrationData: CameraIntrinsics
) {
    val context = LocalContext.current
    LaunchedEffect(calibrationData, frameWidth, frameHeight) {
        calibrationData.cameraMatrix?.let {
            engine.updateCameraProjection(it, frameWidth, frameHeight, calibrationData.calibWidth, calibrationData.calibHeight)
        }
    }

    LaunchedEffect(markers) {
        markers.forEach { marker ->
            val payload = marker.payload
            if (payload is MarkerPayload.Model) {
                val modelName = payload.value.src

                // Проверяем, загружена ли уже модель в движок
                // (Для этого можно добавить метод engine.hasModel(name))

                val modelFile = File(context.filesDir, "models/$modelName")
                if (modelFile.exists()) {
                    engine.loadModel(modelName, modelFile)
                } else {
                    Log.e("AR_VIEW", "Файл модели $modelName не найден в локальном хранилище!")
                }
            }
        }

        engine.updateMarkersPoses(markers)
    }
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {

                setZOrderOnTop(true)
                holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)

                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        engine.attachSurface(eye, holder.surface, width, height)
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        engine.detachSurface(eye)
                    }
                })
            }
        },
        modifier = modifier.fillMaxSize()
    )
}