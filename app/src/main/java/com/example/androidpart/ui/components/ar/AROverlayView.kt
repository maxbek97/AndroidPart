package com.example.androidpart.ui.components.ar


import android.R.attr.textSize
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerPayload

@Composable
fun ArOverlayView(
    modifier: Modifier = Modifier,
    markers: List<ArMarker>
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        markers.forEach { marker ->
            // Отрисовка рамок (corners)
            val corners = marker.corners.firstOrNull() ?: return@forEach
            val path = Path().apply {
                moveTo(corners[0][0], corners[0][1])
                lineTo(corners[1][0], corners[1][1])
                lineTo(corners[2][0], corners[2][1])
                lineTo(corners[3][0], corners[3][1])
                close()
            }
            drawPath(path, Color.Green, style = Stroke(width = 4f))

            // Отрисовка текста
            if (marker.payload is MarkerPayload.Text) {
                val text = marker.payload.value.value
                // Используем nativeCanvas для рисования текста
                drawContext.canvas.nativeCanvas.drawText(
                    text,
                    corners[0][0],
                    corners[0][1] - 20f,
                    Paint().apply { color = android.graphics.Color.WHITE; textSize = 40f }
                )
            }
        }
    }
}