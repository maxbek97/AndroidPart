package com.example.androidpart.ui.components.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import com.example.androidpart.data.remote.WsClient
import com.example.androidpart.data.remote.arJson
import com.example.androidpart.domain.model.WsMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FrameAnalyzer(
    private val wsClient: WsClient,
    private val onFrameConverted: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastTime = 0L
    private var frameCounter = 0

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()

        if (now - lastTime < 100) {
            image.close()
            return
        }
        try {
            val bitmap = image.toBitmap()
            // 1. Создаем Bitmap
            onFrameConverted(bitmap) // Отправляем в UI
            val outputStream = ByteArrayOutputStream()
            // 80 - хороший баланс между качеством ArUco и размером пакета
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val jpegBytes = outputStream.toByteArray()

            val base64Image = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
            val payload: WsMessage = WsMessage.Frame(
                frame_id = frameCounter++,
                image = base64Image
            )

            val jsonString = arJson.encodeToString<WsMessage>(payload)
            wsClient.send(jsonString)
        }
        catch (e: Exception) {
        }
        finally {
            lastTime = now
            image.close()
        }

    }
}