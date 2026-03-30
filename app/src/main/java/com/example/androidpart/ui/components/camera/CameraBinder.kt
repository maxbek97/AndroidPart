package com.example.androidpart.ui.components.camera

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

fun bindCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    leftView: PreviewView,
    rightView: PreviewView,
    targetSize: Size
) {
    val cameraProvider = ProcessCameraProvider.getInstance(context).get()

    val resolutionSelector = ResolutionSelector.Builder()
        .setResolutionStrategy(
            ResolutionStrategy(
                targetSize,
                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
            )
        )
        .build()

    val previewLeft = Preview.Builder()
        .setResolutionSelector(resolutionSelector)
        .build()

    val previewRight = Preview.Builder()
        .setResolutionSelector(resolutionSelector)
        .build()

    previewLeft.setSurfaceProvider(leftView.surfaceProvider)
    previewRight.setSurfaceProvider(rightView.surfaceProvider)

    cameraProvider.unbindAll()

    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        previewLeft,
        previewRight
    )
}