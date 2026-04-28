package com.example.androidpart.ui.screens.CalibrationScreen

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

fun PreviewView.setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onFrame: (Boolean) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(this.surfaceProvider)
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(
            ContextCompat.getMainExecutor(context)
        ) { imageProxy ->

            // 👇 пока заглушка
            val detected = fakeDetectChessboard()

            onFrame(detected)

            imageProxy.close()
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            analysis
        )

    }, ContextCompat.getMainExecutor(context))
}

fun fakeDetectChessboard(): Boolean {
    return (0..10).random() > 3
}

fun generateFakePoints(): List<Pair<Float, Float>> {
    return List(20) { Pair(Math.random().toFloat(), Math.random().toFloat()) }
}

fun performCalibration(points: List<List<Pair<Float, Float>>>) {
    println("Калибровка на ${points.size} кадрах")
}

fun getHintText(count: Int): String {
    return when {
        count < 3 -> "Держите доску прямо перед камерой"
        count < 7 -> "Наклоните доску под углом"
        count < 11 -> "Поднесите ближе/дальше"
        else -> "Отлично! Еще немного"
    }
}