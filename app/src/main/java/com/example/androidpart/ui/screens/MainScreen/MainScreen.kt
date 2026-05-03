package com.example.androidpart.ui.screens.MainScreen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.util.Size
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.androidpart.rendering.filament.Eye
import com.example.androidpart.ui.components.ar.ArEyeContainer
import com.example.androidpart.ui.components.camera.bindCamera
import com.example.androidpart.ui.components.camera.rememberCameraPreviewView

@Composable
fun MainScreen(
    navHostController: NavHostController,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val engine = viewModel.engine

    val markers by viewModel.markers.collectAsState()
    val currentFrame by viewModel.currentFrame.collectAsState()

    LaunchedEffect(engine) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                engine.render(frameTimeNanos)
            }
        }
    }

    val activity = context as? Activity

    val targetSize = Size(1080, 1080)

    DisposableEffect(Unit) {
        val window = activity?.window
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val previewView = rememberCameraPreviewView(context)
    LaunchedEffect(Unit) {
        bindCamera(context,
            lifecycleOwner,
            previewView,
            targetSize,
            viewModel.wsClient) { bitmap ->
            viewModel.updateFrame(bitmap)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(modifier = Modifier.size(1.dp).alpha(0f)) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            // Левый глаз
            ArEyeContainer(
                modifier = Modifier.weight(1f),
                markers = markers,
                frame = currentFrame,
                engine = engine,
                eye = Eye.LEFT
            )

            // Правый глаз
            ArEyeContainer(
                modifier = Modifier.weight(1f),
                markers = markers,
                frame = currentFrame,
                engine = engine,
                eye = Eye.RIGHT
            )

        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .align(Alignment.Center)
                .background(Color.Red)
        )
    }
}