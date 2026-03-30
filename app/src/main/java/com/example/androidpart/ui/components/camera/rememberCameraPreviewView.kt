package com.example.androidpart.ui.components.camera

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberCameraPreviewView(context: Context): PreviewView {
    return remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
}