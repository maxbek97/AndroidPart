package com.example.androidpart.ui.screens.CalibrationScreen.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.androidpart.ui.screens.CalibrationScreen.opencv.ChessboardDetector
import com.example.androidpart.ui.screens.CalibrationScreen.opencv.DetectionResult
import com.example.androidpart.ui.screens.CalibrationScreen.opencv.ImageProxyConverter


class FrameAnalyzer(
    private val detector: ChessboardDetector,
    private val onResult: (DetectionResult) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {

        val gray = ImageProxyConverter.imageToGrayMat(image)

        val result = detector.detect(gray)

        onResult(result)

        image.close()
    }
}