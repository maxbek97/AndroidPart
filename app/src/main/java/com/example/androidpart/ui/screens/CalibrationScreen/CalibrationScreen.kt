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

@Composable
fun CalibrationScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val requiredPhotos = 15
    var photosTaken by remember { mutableStateOf(0) }
    var chessboardDetected by remember { mutableStateOf(false) }

    // сюда будем складывать точки
    val imagePoints = remember { mutableStateListOf<List<Pair<Float, Float>>>() }

    Box(modifier = Modifier.fillMaxSize()) {

        // 📷 Камера
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    setupCamera(
                        context = ctx,
                        lifecycleOwner = lifecycleOwner,
                        onFrame = { detected ->
                            chessboardDetected = detected
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 🧾 Текст сверху
        Text(
            text = getHintText(photosTaken),
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp, vertical = 54.dp)
        )

        // 🔘 Кнопка + прогресс
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
                    .background(if (chessboardDetected) Color.White else Color.Gray)
                    .clickable {

                        if (!chessboardDetected) return@clickable

                        // 👇 сохраняем "кадр"
                        imagePoints.add(generateFakePoints())

                        photosTaken++

                        if (photosTaken >= requiredPhotos) {
                            // 👉 тут будет калибровка
                            performCalibration(imagePoints)

                            imagePoints.clear()

                            navController.popBackStack()
                        }
                    }
            )
        }
    }
}