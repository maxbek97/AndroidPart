package com.example.androidpart.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidpart.R
import androidx.core.net.toUri

@androidx.media3.common.util.UnstableApi
@Composable
fun VideoBackground() {

    val context = LocalContext.current

    val exoPlayer = remember {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {

            val videoUri = "android.resource://${context.packageName}/${R.raw.webvid}".toUri()

            val mediaItem = androidx.media3.common.MediaItem.fromUri(videoUri)

            setMediaItem(mediaItem)
            repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            androidx.media3.ui.PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode =
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}