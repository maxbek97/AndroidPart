package com.example.androidpart.ui.components.camera

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraEyeView(
    modifier: Modifier = Modifier,
    previewView: PreviewView
) {
    AndroidView(
        factory = { previewView },
        modifier = modifier
            .fillMaxHeight()
    )
}