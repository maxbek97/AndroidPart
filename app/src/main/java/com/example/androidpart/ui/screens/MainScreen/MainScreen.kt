package com.example.androidpart.ui.screens.MainScreen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalConfiguration
import android.os.Build
import android.view.WindowManager
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun MainScreen(navHostController: NavHostController) {

    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val targetSize = Size(1080, 1080)

    // FULLSCREEN
    DisposableEffect(Unit) {
        activity?.let {
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            val window = it.window
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val params = window.attributes
                params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = params
            }
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Р”Р’Рђ PreviewView
    val leftView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    val rightView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    LaunchedEffect(Unit) {

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

        try {
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewLeft,
                previewRight
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // 50/50 РєРѕРЅС‚РµРЅС‚
        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            AndroidView(
                factory = { leftView },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            AndroidView(
                factory = { rightView },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // рџ”ґ Р¦РµРЅС‚СЂР°Р»СЊРЅР°СЏ Р»РёРЅРёСЏ (РїРѕРІРµСЂС…!)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .align(Alignment.Center)
                .background(Color.Red)
        )
    }
}