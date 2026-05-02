package com.example.androidpart.ui.screens.MainScreen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import android.view.WindowManager
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.androidpart.data.local.ModelManager
import com.example.androidpart.data.local.SettingsDataStore
import com.example.androidpart.data.remote.WsClient
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerPayload
import com.example.androidpart.domain.model.MarkerResponse
import com.example.androidpart.domain.model.WsMessage
import com.example.androidpart.ui.components.ar.ArEyeContainer
import com.example.androidpart.ui.components.camera.CameraEyeView
import com.example.androidpart.ui.components.camera.bindCamera
import com.example.androidpart.ui.components.camera.rememberCameraPreviewView
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun MainScreen(navHostController: NavHostController) {

    val context = LocalContext.current
    val settings = remember { SettingsDataStore(context) }
    var currentFrame by remember { mutableStateOf<Bitmap?>(null) }
    val wsClient = remember { WsClient() }

    val markersState = remember { mutableStateOf<List<ArMarker>>(emptyList()) }
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        wsClient.connect { message ->
            try {
                val response = arJson.decodeFromString<MarkerResponse>(message)
                markersState.value = response.markers
                // Логирование для проверки типов
            } catch (e: Exception) {
                Log.e("AR_DEBUG", "Parse error: ${e.message}")
            }
        }


        // --- ЛОГИКА ИНИЦИАЛИЗАЦИИ ---
        // Ждем получения калибровки и отправляем Init пакет
        val calibration = settings.calibrationFlow.first()
        val currentSettings = settings.settingsFlow.first()

        val matrixStr = calibration.first
        val distStr = calibration.second
        val markerSize = currentSettings.third

        if (matrixStr != null && distStr != null) {
            val initPacket = WsMessage.Init(
                camera_matrix = parseStringMatrix(matrixStr),
                dist_coeffs = parseStringList(distStr),
                marker_length = markerSize.toDouble()
            )
            val jsonInit = Json.encodeToString(initPacket)
            Log.d("AR_DEBUG", "Sending INIT: $jsonInit")
            wsClient.send(jsonInit)
        }
    }


    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val targetSize = Size(1080, 1080)

    DisposableEffect(Unit) {
        val window = activity?.window
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            Log.d("UI_DEBUG", "MainScreen disposed: cleaning up...")
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            wsClient.disconnect()
        }
    }

    // Р”Р’Рђ PreviewView
    val previewView = rememberCameraPreviewView(context)
    LaunchedEffect(Unit) {
        bindCamera(
            context,
            lifecycleOwner,
            previewView, // Передаем первый
            targetSize,
            wsClient
        ) { bitmap ->
            // КАМЕРА РАБОТАЕТ В СВОЕМ ПОТОКЕ, НУЖНО ВЕРНУТЬСЯ В MAIN ДЛЯ UI
            (context as Activity).runOnUiThread {
                Log.d("UI_DEBUG", "New frame received in UI: ${bitmap.byteCount} bytes")
                currentFrame = bitmap
            }// Обновляем кадр для обоих глаз сразу
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(modifier = Modifier.size(1.dp).alpha(0f)) {
            CameraEyeView(previewView = previewView)
        }

        // 50/50 РєРѕРЅС‚РµРЅС‚
        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            // Левый глаз
            ArEyeContainer(
                modifier = Modifier.weight(1f),
                markers = markersState.value,
                frame = currentFrame
            )

            // Правый глаз
            ArEyeContainer(
                modifier = Modifier.weight(1f),
                markers = markersState.value,
                frame = currentFrame
            )

        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .align(Alignment.Center)
                .background(Color.Red)
        )
    }
}
fun parseStringMatrix(input: String): List<List<Double>> {
    return try {
        // 1. Разбиваем строку на ряды по точке с запятой
        input.split(';')
            .filter { it.isNotBlank() }
            .map { row ->
                // 2. Каждый ряд разбиваем на числа по запятой
                row.split(',')
                    .filter { it.isNotBlank() }
                    .map { it.trim().toDouble() }
            }
    } catch (e: Exception) {
        Log.e("CAMERA_CALIB", "Ошибка парсинга матрицы 3x3: ${e.message}")
        // Возвращаем пустой вложенный список в случае ошибки
        emptyList()
    }
}

// Делаем parseStringList таким же безопасным
fun parseStringList(str: String): List<Double> {
    return try {
        // Если строка начинается со скобки, парсим как JSON, иначе — как CSV
        if (str.trim().startsWith("[")) {
            Json.decodeFromString<List<Double>>(str)
        } else {
            str.split(',')
                .filter { it.isNotBlank() }
                .map { it.trim().toDouble() }
        }
    } catch (e: Exception) {
        Log.e("CAMERA_CALIB", "Ошибка парсинга списка: ${e.message}")
        emptyList()
    }
}

val arJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
    // Это важно для десериализации sealed classes
    classDiscriminator = "type"
}