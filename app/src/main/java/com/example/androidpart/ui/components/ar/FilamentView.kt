package com.example.androidpart.ui.components.ar

import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidpart.data.local.ModelManager
import com.example.androidpart.domain.ar.PoseMapper
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerPayload
import com.example.androidpart.rendering.filament.Eye
import com.example.androidpart.rendering.filament.FilamentEngine
import java.io.File

@Composable
fun FilamentView(
    modifier: Modifier = Modifier,
    markers: List<ArMarker>,
    engine: FilamentEngine,
    eye: Eye
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {

                setZOrderOnTop(true)
                holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)

                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        engine.attachSurface(eye, holder.surface, width, height)

                        val file = File(context.filesDir, "models/banana.glb")
                        if (file.exists()) {
                            engine.loadModel("test", file)
                            engine.placeModelInFront("test")
                        }
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {

                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        engine.detachSurface(eye)
                    }
                })
            }
        },
        modifier = modifier.fillMaxSize()
    )
}