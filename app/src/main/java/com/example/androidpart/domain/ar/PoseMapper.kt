package com.example.androidpart.domain.ar

// domain/ar/PoseMapper.kt
object PoseMapper {
    fun toFilamentMatrix(rvec: List<Double>, tvec: List<Double>): FloatArray {
        // 1. Превращаем rvec (вектор Родрига) в матрицу вращения 3x3
        // Для этого обычно используют функцию Rodrigues из OpenCV (через библиотеку или вручную)
        val rotationMatrix = rodriguesToRotationMatrix(rvec)

        // 2. Формируем матрицу 4x4 для Filament (Column-major order)
        val matrix = FloatArray(16)

        // Матрица вращения и трансляции
        matrix[0] = rotationMatrix[0]; matrix[4] = rotationMatrix[1]; matrix[8] = rotationMatrix[2];  matrix[12] = tvec[0].toFloat()
        matrix[1] = rotationMatrix[3]; matrix[5] = rotationMatrix[4]; matrix[9] = rotationMatrix[5];  matrix[13] = tvec[1].toFloat()
        matrix[2] = rotationMatrix[6]; matrix[6] = rotationMatrix[7]; matrix[10] = rotationMatrix[8]; matrix[14] = tvec[2].toFloat()
        matrix[3] = 0f;                matrix[7] = 0f;                matrix[11] = 0f;                matrix[15] = 1f

        return matrix
    }

    private fun rodriguesToRotationMatrix(rvec: List<Double>): FloatArray {
        val theta = Math.sqrt(rvec[0]*rvec[0] + rvec[1]*rvec[1] + rvec[2]*rvec[2])
        if (theta < 1e-6) return floatArrayOf(1f,0f,0f, 0f,1f,0f, 0f,0f,1f)

        val r = rvec.map { (it / theta).toFloat() }
        val cosT = Math.cos(theta).toFloat()
        val sinT = Math.sin(theta).toFloat()
        val out = FloatArray(9)
        // Формула Родрига...
        return out
    }
}