package com.example.androidpart.ui.screens.MainScreen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
@Composable
fun MainScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current


    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            navHostController.navigate("error/camera") {
                popUpTo("main") { inclusive = true }
            }
        }
    }

    // ❗ не даём дальше выполняться
    if (!hasCameraPermission) return

    // Фиксируем горизонтальную ориентацию
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // PreviewView для глаз
    val leftEyePreview = remember { PreviewView(context) }
    val rightEyePreview = remember { PreviewView(context) }

    // CameraProvider
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()

        // Настройка Preview для левого глаза
        val previewLeft = Preview.Builder()
            .setTargetResolution(Size(1080, 1200))
            .build()
            .also { it.setSurfaceProvider(leftEyePreview.surfaceProvider) }

        // Настройка Preview для правого глаза
        val previewRight = Preview.Builder()
            .setTargetResolution(Size(1080, 1200))
            .build()
            .also { it.setSurfaceProvider(rightEyePreview.surfaceProvider) }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // Развязываем все старые камеры
        cameraProvider.unbindAll()

        // Привязываем к текущему LifecycleOwner
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            previewLeft,
            previewRight
        )
    }

    // Размещение PreviewView на экране
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AndroidView(
            factory = { leftEyePreview },
            modifier = Modifier
                .weight(1f)
                .aspectRatio(9f / 10f)
        )
        AndroidView(
            factory = { rightEyePreview },
            modifier = Modifier
                .weight(1f)
                .aspectRatio(9f / 10f)
        )
    }
}