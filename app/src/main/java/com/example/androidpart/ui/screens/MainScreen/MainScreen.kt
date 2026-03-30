package com.example.androidpart.ui.screens.MainScreen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.androidpart.data.remote.WsClient
import com.example.androidpart.ui.components.camera.CameraEyeView
import com.example.androidpart.ui.components.camera.bindCamera
import com.example.androidpart.ui.components.camera.rememberCameraPreviewView

@Composable
fun MainScreen(navHostController: NavHostController) {

    val wsClient = remember { WsClient() }

    LaunchedEffect(Unit) {
        wsClient.connect { message ->
            Log.d("WS", message)
        }
    }

    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val targetSize = Size(1080, 1080)

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Р”Р’Рђ PreviewView
    val previewView = rememberCameraPreviewView(context)

    LaunchedEffect(Unit) {
        bindCamera(
            context,
            lifecycleOwner,
            previewView,
            targetSize,
            wsClient
        )
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

            CameraEyeView(
                previewView = previewView,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.DarkGray)
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