package com.example.androidpart.ui.screens.CalibrationScreen.calibration

import org.opencv.calib3d.Calib3d
import org.opencv.core.*

class CalibrationManager {

    private val objPoints = mutableListOf<Mat>()
    private val imgPoints = mutableListOf<Mat>()

    private val patternSize = Size(7.0, 7.0)

    fun addFrame(corners: MatOfPoint2f) {

        imgPoints.add(corners)

        val objp = MatOfPoint3f()

        val points = mutableListOf<Point3>()

        for (i in 0 until patternSize.height.toInt()) {
            for (j in 0 until patternSize.width.toInt()) {
                points.add(Point3(j.toDouble(), i.toDouble(), 0.0))
            }
        }

        objp.fromList(points)

        objPoints.add(objp)
    }

    fun calibrate(imageSize: Size): Pair<Mat, Mat> {

        val cameraMatrix = Mat.eye(3, 3, CvType.CV_64F)
        val distCoeffs = Mat.zeros(8, 1, CvType.CV_64F)

        Calib3d.calibrateCamera(
            objPoints,
            imgPoints,
            imageSize,
            cameraMatrix,
            distCoeffs,
            mutableListOf(),
            mutableListOf()
        )

        return Pair(cameraMatrix, distCoeffs)
    }

    fun clear() {
        objPoints.clear()
        imgPoints.clear()
    }
}