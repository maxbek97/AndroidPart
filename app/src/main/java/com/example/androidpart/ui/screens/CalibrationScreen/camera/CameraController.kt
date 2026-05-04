package com.example.androidpart.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import android.util.Size
import java.util.concurrent.Executors
class CameraController {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    fun bind(
        context: Context,
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        analyzer: ImageAnalysis.Analyzer,
        targetSize: Size
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetResolution(targetSize)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(targetSize)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                cameraExecutor,
                analyzer
            )

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                Log.d("CAMERA_CONTROLLER", "Bound successfully with resolution: $targetSize")
            } catch (e: Exception) {
                Log.e("CAMERA_CONTROLLER", "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(context))
    }
    fun shutdown() {
        cameraExecutor.shutdown()
    }
}