package com.example.androidpart.ui.screens.CalibrationScreen.opencv

import org.opencv.calib3d.Calib3d
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

data class DetectionResult(
    val found: Boolean,
    val corners: MatOfPoint2f?
)

class ChessboardDetector {

//  Количество клеток на доске по ширине/длине. В передаче в функцию должно быть на одну меньше
    val COUNT_CELL: Double = 8.0

//    Доска квадратная
    private val patternSize = Size(COUNT_CELL - 1, COUNT_CELL - 1) // для 8x8 доски

    fun detect(gray: Mat): DetectionResult {

        val corners = MatOfPoint2f()

        val found = Calib3d.findChessboardCorners(
            gray,
            patternSize,
            corners
        )

        if (found) {
            Imgproc.cornerSubPix(
                gray,
                corners,
                Size(11.0, 11.0),
                Size(-1.0, -1.0),
                org.opencv.core.TermCriteria(
                    org.opencv.core.TermCriteria.EPS + org.opencv.core.TermCriteria.MAX_ITER,
                    30,
                    0.1
                )
            )
        }

        return DetectionResult(found, if (found) corners else null)
    }
}