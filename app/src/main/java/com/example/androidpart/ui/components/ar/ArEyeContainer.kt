package com.example.androidpart.ui.components.ar

import android.graphics.Bitmap
import android.util.Log
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
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.CameraIntrinsics
import com.example.androidpart.domain.ar.Eye
import com.example.androidpart.domain.ar.FilamentEngine

@Composable
fun ArEyeContainer(
    modifier: Modifier = Modifier,
    markers: List<ArMarker>,
    frame: Bitmap?,
    engine: FilamentEngine,
    eye: Eye,
    calibrationData: CameraIntrinsics
) {
    val actualWidth = frame?.width?.toFloat() ?: calibrationData.calibWidth
    val actualHeight = frame?.height?.toFloat() ?: calibrationData.calibHeight

    Box(modifier = modifier.fillMaxHeight().background(Color.Blue)) {

        //Слой 1, слой камеры
        frame?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        // Слой 2: 3D Графика Filament
        FilamentView(
            markers = markers,
            engine = engine,
            eye = eye,
            frameWidth = actualWidth,
            frameHeight = actualHeight,
            calibrationData = calibrationData
        )

        // Слой 3: 2D Текст и рамки
        ArOverlayView(
            modifier = Modifier.fillMaxSize(),
            markers = markers,
            frameWidth = actualWidth,
            frameHeight = actualHeight
        )
    }
}