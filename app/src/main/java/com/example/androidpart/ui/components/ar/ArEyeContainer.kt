package com.example.androidpart.ui.components.ar

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.androidpart.data.local.ModelManager
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.rendering.filament.Eye
import com.example.androidpart.rendering.filament.FilamentEngine

@Composable
fun ArEyeContainer(
    modifier: Modifier = Modifier,
    markers: List<ArMarker>,
    frame: Bitmap?,
    engine: FilamentEngine,
    eye: Eye,
    frameWidth: Float,  // FallBack
    frameHeight: Float,
    cameraMatrix: List<List<Double>>?
) {
    val actualWidth = frame?.width?.toFloat() ?: frameWidth
    val actualHeight = frame?.height?.toFloat() ?: frameHeight

    // Добавь этот Side Effect для отладки
    LaunchedEffect(actualWidth, actualHeight, frameWidth, frameHeight) {
        Log.d("AR_DEBUG_DIMENS", """
        [EYE: $eye] 
        Settings (Calib): ${frameWidth}x${frameHeight}
        Actual Bitmap: ${actualWidth}x${actualHeight}
        Scale Diff: X:${actualWidth/frameWidth} Y:${actualHeight/frameHeight}
    """.trimIndent())
    }

    Box(modifier = modifier.fillMaxHeight().background(Color.Blue)) {

        //Слой 0, слой камеры
        frame?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        // Слой 1: 3D Графика Filament (прозрачный SurfaceView поверх) Он изза него делает черным экран, но при этом детекция работает
        FilamentView(
            markers = markers,
            engine = engine,
            eye = eye,
            frameWidth = actualWidth,
            frameHeight = actualHeight,
            cameraMatrix = cameraMatrix,
            calibWidth = frameWidth,
            calibHeight = frameHeight
        )

        // Слой 2: 2D Текст и рамки (самый верхний)
        ArOverlayView(
            modifier = Modifier.fillMaxSize(),
            markers = markers,
            frameWidth = actualWidth,
            frameHeight = actualHeight
        )
    }
}