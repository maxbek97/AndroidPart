package com.example.androidpart.ui.components.camera

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import android.util.Base64
import com.example.androidpart.data.remote.WsClient
import com.example.androidpart.domain.model.FramePayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FrameAnalyzer(
    private val wsClient: WsClient
) : ImageAnalysis.Analyzer {

    private var lastTime = 0L
    private var frameCounter = 0

    override fun analyze(image: ImageProxy) {
        val now = System.currentTimeMillis()

        if (now - lastTime < 100) { // ~10 FPS
            image.close()
            return
        }

        val jpeg = imageProxyToJpeg(image)
        val base64Image = Base64.encodeToString(jpeg, Base64.DEFAULT)

        val payload = FramePayload(
            frame_id = frameCounter++,
            image = base64Image
        )

        val jsonString = Json.encodeToString(payload)

        wsClient.send(jsonString)

        lastTime = now
        image.close()
    }
}
fun imageProxyToJpeg(image: ImageProxy): ByteArray {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val nv21 = ByteArray(yBuffer.remaining() + uBuffer.remaining() + vBuffer.remaining())

    yBuffer.get(nv21, 0, yBuffer.remaining())
    vBuffer.get(nv21, yBuffer.remaining(), vBuffer.remaining())
    uBuffer.get(nv21, yBuffer.remaining() + vBuffer.remaining(), uBuffer.remaining())

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)

    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 70, out)

    return out.toByteArray()
}