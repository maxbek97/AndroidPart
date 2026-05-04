package com.example.androidpart.ui.components.ar


import android.R.attr.textSize
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.androidpart.domain.ar.getScaleFactors
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerPayload

@Composable
fun ArOverlayView(
    modifier: Modifier = Modifier,
    markers: List<ArMarker>,
    frameWidth: Float = 640f,
    frameHeight: Float = 480f
) {
    LaunchedEffect(frameWidth, frameHeight) {
        Log.d("AR_DEBUG_OVERLAY", "Overlay configured for Frame: ${frameWidth}x${frameHeight}")
    }
    Canvas(modifier = modifier.fillMaxSize()) {

        val canvasWidth = size.width
        val canvasHeight = size.height
        // ПРИМЕНЯЕМ ТВОЮ ФУНКЦИЮ
        val (scale, offset) = getScaleFactors(
            contentWidth = frameWidth,
            contentHeight = frameHeight,
            viewWidth = canvasWidth,
            viewHeight = canvasHeight
        )


        markers.forEach { marker ->
            // Отрисовка рамок (corners)
            val corners = marker.corners.firstOrNull() ?: return@forEach
            // Трансформируем все 4 угла сразу
            val p0x = corners[0][0] * scale + offset.x
            val p0y = corners[0][1] * scale + offset.y
            val p1x = corners[1][0] * scale + offset.x
            val p1y = corners[1][1] * scale + offset.y
            val p2x = corners[2][0] * scale + offset.x
            val p2y = corners[2][1] * scale + offset.y
            val p3x = corners[3][0] * scale + offset.x
            val p3y = corners[3][1] * scale + offset.y

            val lineLength = 40f
            val cornerStrokeWidth = 6f
            val cornerColor = Color.White

            fun drawCorner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(cornerColor, Offset(x, y), Offset(x + dx * lineLength, y), cornerStrokeWidth)
                drawLine(cornerColor, Offset(x, y), Offset(x, y + dy * lineLength), cornerStrokeWidth)
            }

            // Рисуем 4 угла (направления dx, dy зависят от того, куда должен "смотреть" уголок)
            drawCorner(p0x, p0y, 1f, 1f)   // Верхний левый
            drawCorner(p1x, p1y, -1f, 1f)  // Верхний правый
            drawCorner(p2x, p2y, -1f, -1f) // Нижний правый
            drawCorner(p3x, p3y, 1f, -1f)  // Нижний левый

            // Отрисовка текста
            if (marker.payload is MarkerPayload.Text) {
                val text = marker.payload.value.value

                // Вычисляем центр (среднее арифметическое координат)
                val centerX = (p0x + p1x + p2x + p3x) / 4f
                val centerY = (p0y + p1y + p2y + p3y) / 4f

                val fixedWidth = 400

                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.translate(centerX, centerY)

                val basePaint = TextPaint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 35f // Уменьшил шрифт, чтобы влезло больше
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }

                val strokePaint = TextPaint(basePaint).apply {
                    color = android.graphics.Color.BLACK
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 6f // Толщина обводки
                }

                val layoutStroke = StaticLayout.Builder.obtain(text, 0, text.length, strokePaint, fixedWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()

                // Создаем Layout для ОСНОВНОГО ТЕКСТА (белый)
                val fillPaint = TextPaint(basePaint).apply {
                    color = android.graphics.Color.WHITE
                    style = android.graphics.Paint.Style.FILL
                }

                val layoutFill = StaticLayout.Builder.obtain(text, 0, text.length, fillPaint, fixedWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()

// Центрируем по вертикали
                val totalHeight = layoutFill.height
                drawContext.canvas.nativeCanvas.translate(0f, -totalHeight / 2f)

                // Рисуем сначала обводку, потом заливку
                layoutStroke.draw(drawContext.canvas.nativeCanvas)
                layoutFill.draw(drawContext.canvas.nativeCanvas)

                drawContext.canvas.nativeCanvas.restore()



            }
        }
    }
}