package com.example.androidpart.domain.ar

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// domain/ar/PoseMapper.kt
object PoseMapper {
    fun toFilamentMatrix(rvec: List<Double>, tvec: List<Double>): FloatArray {
        // 1. Превращаем rvec (вектор Родрига) в матрицу вращения 3x3
        // Для этого обычно используют функцию Rodrigues из OpenCV (через библиотеку или вручную)
        val rotationMatrix = rodriguesToRotationMatrix(rvec)

        // 2. Формируем матрицу 4x4 для Filament (Column-major order)
        val openCVMatrix = FloatArray(16).apply {
            this[0] = rotationMatrix[0]; this[4] = rotationMatrix[1]; this[8] = rotationMatrix[2];  this[12] = tvec[0].toFloat()
            this[1] = -rotationMatrix[3]; this[5] = -rotationMatrix[4]; this[9] = -rotationMatrix[5];  this[13] = -tvec[1].toFloat()
            this[2] = -rotationMatrix[6]; this[6] = -rotationMatrix[7]; this[10] = -rotationMatrix[8]; this[14] = -tvec[2].toFloat()
            this[3] = 0f;                this[7] = 0f;                this[11] = 0f;                this[15] = 1f
        }
        val correctionMatrix = FloatArray(16)
        android.opengl.Matrix.setIdentityM(correctionMatrix, 0)
        android.opengl.Matrix.rotateM(correctionMatrix, 0, 90f, 1f, 0f, 0f)

        val resultMatrix = FloatArray(16)
        // Важно: умножаем Исходную на Коррекцию, чтобы поворот был локальным для модели
        android.opengl.Matrix.multiplyMM(resultMatrix, 0, openCVMatrix, 0, correctionMatrix, 0)

        return resultMatrix
    }

    private fun rodriguesToRotationMatrix(rvec: List<Double>): FloatArray {
        val theta = sqrt(rvec[0] * rvec[0] + rvec[1] * rvec[1] + rvec[2] * rvec[2])
        val out = FloatArray(9)
        if (theta < 1e-6) {
            return floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
        }

        val ux = (rvec[0] / theta).toFloat()
        val uy = (rvec[1] / theta).toFloat()
        val uz = (rvec[2] / theta).toFloat()

        val c = cos(theta).toFloat()
        val s = sin(theta).toFloat()
        val t = 1f - c

        out[0] = c + ux * ux * t
        out[1] = ux * uy * t - uz * s
        out[2] = ux * uz * t + uy * s

        out[3] = uy * ux * t + uz * s
        out[4] = c + uy * uy * t
        out[5] = uy * uz * t - ux * s

        out[6] = uz * ux * t - uy * s
        out[7] = uz * uy * t + ux * s
        out[8] = c + uz * uz * t

        return out
    }
}