package com.example.androidpart.domain.model

data class CameraIntrinsics(
    val cameraMatrix: List<List<Double>>,
    val distCoeffs: List<Double>,
    val calibWidth: Float,
    val calibHeight: Float
)