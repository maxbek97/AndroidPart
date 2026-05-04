package com.example.androidpart.ui.screens.CalibrationScreen

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.androidpart.camera.CameraController
import com.example.androidpart.ui.screens.CalibrationScreen.calibration.CalibrationManager
import com.example.androidpart.ui.screens.CalibrationScreen.camera.FrameAnalyzer
import com.example.androidpart.ui.screens.CalibrationScreen.opencv.ChessboardDetector
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.androidpart.data.local.SettingsDataStore
import kotlinx.coroutines.launch
import org.opencv.core.Mat

@Composable
fun CalibrationScreen(navController: NavHostController) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val detector = remember { ChessboardDetector() }
    val calibrationManager = remember { CalibrationManager() }
    val cameraController = remember { CameraController() }
    val settingsDataStore = remember { SettingsDataStore(context) }

    val settings by settingsDataStore.settingsFlow.collectAsState(initial = null)

    val targetSize = remember(settings) {
        settings?.first?.split("x")?.let {
            android.util.Size(it[0].toInt(), it[1].toInt())
        }
    }
    var detected by remember { mutableStateOf(false) }
    var currentCorners by remember { mutableStateOf<MatOfPoint2f?>(null) }
    var photosTaken by remember { mutableStateOf(0) }
    val requiredPhotos = 15

    val previewView = remember { PreviewView(context) }
    val analyzer = remember {
        FrameAnalyzer(detector) { result ->
            detected = result.found
            currentCorners = result.corners
        }
    }

    LaunchedEffect(targetSize) {
        // ЗАПУСКАЕМ ТОЛЬКО КОГДА ЕСТЬ РЕАЛЬНЫЙ РАЗМЕР
        if (targetSize != null) {
            Log.d("CALIB_DEBUG", "Starting calibration camera with target: ${targetSize.width}x${targetSize.height}")
            cameraController.bind(
                context = context,
                previewView = previewView,
                lifecycleOwner = lifecycleOwner,
                analyzer = analyzer,
                targetSize = targetSize
            )
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            Log.d("CALIB_DEBUG", "Cleaning up CameraController")
            cameraController.shutdown()
        }
    }

    // Добавляем эффект, который перезапускает камеру с нужным размером

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = getHintText(photosTaken),
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp, vertical = 54.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {

            val progress = photosTaken / requiredPhotos.toFloat()
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(90.dp),
                strokeWidth = 6.dp,
                color = Color.Green
            )

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(if (detected) Color.White else Color.Gray)
                    .clickable {
                        val size = targetSize
                        if (!detected || currentCorners == null || size == null) return@clickable


                        calibrationManager.addFrame(currentCorners!!)
                        photosTaken++

                        if (photosTaken >= requiredPhotos) {
                            val calibrationSize = Size(targetSize.width.toDouble(), targetSize.height.toDouble())
                            val (cameraMatrix, distCoeffs) = calibrationManager.calibrate(calibrationSize)


                            scope.launch {
                                settingsDataStore.saveCalibration(
                                    matToString(cameraMatrix),
                                    matToString(distCoeffs)
                                )
                                calibrationManager.clear()
                                navController.popBackStack()
                            }
                        }
                    }
            )
        }
    }
}

private fun matToString(mat: Mat): String {
    val sb = StringBuilder()

    for (i in 0 until mat.rows()) {
        for (j in 0 until mat.cols()) {
            sb.append(mat.get(i, j)[0])
            if (j < mat.cols() - 1) sb.append(",")
        }
        if (i < mat.rows() - 1) sb.append(";")
    }

    return sb.toString()
}

private fun getHintText(count: Int): String {
    return when {
        count < 3 -> "Держите доску прямо перед камерой"
        count < 7 -> "Наклоните доску под углом"
        count < 11 -> "Поднесите ближе/дальше"
        else -> "Отлично! Еще немного"
    }
}