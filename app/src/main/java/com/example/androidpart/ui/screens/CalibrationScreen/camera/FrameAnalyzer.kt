package com.example.androidpart.ui.screens.CalibrationScreen.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.androidpart.ui.screens.CalibrationScreen.opencv.ChessboardDetector
import com.example.androidpart.ui.screens.CalibrationScreen.opencv.DetectionResult
import com.example.androidpart.ui.screens.CalibrationScreen.opencv.ImageProxyConverter


class FrameAnalyzer(
    private val detector: ChessboardDetector,
    private val onResult: (DetectionResult, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val width = image.width
        val height = image.height
        val gray = ImageProxyConverter.imageToGrayMat(image)
        Log.d("CALIB_FRAME", "Frame received: ${image.width}x${image.height}")
        val result = detector.detect(gray)

        onResult(result, width, height)

        image.close()
    }
}