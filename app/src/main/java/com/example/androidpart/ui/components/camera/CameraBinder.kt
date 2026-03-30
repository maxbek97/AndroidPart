package com.example.androidpart.ui.components.camera

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.androidpart.data.remote.WsClient

fun bindCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    targetSize: Size,
    wsClient: WsClient
) {
    val imageAnalyzer = ImageAnalysis.Builder()
        .setTargetResolution(targetSize)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val cameraProvider = ProcessCameraProvider.getInstance(context).get()

    val resolutionSelector = ResolutionSelector.Builder()
        .setResolutionStrategy(
            ResolutionStrategy(
                targetSize,
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
            )
        )
        .build()


    val preview = Preview.Builder()
        .setResolutionSelector(resolutionSelector)
        .build()

    preview.setSurfaceProvider(previewView.surfaceProvider)

    cameraProvider.unbindAll()

    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
        imageAnalyzer
    )

    imageAnalyzer.setAnalyzer(
        ContextCompat.getMainExecutor(context),
        FrameAnalyzer(wsClient)
    )
}