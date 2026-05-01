package com.example.androidpart.ui.screens.MainScreen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.androidpart.data.local.SettingsDataStore
import com.example.androidpart.data.remote.WsClient
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerPayload
import com.example.androidpart.domain.model.MarkerResponse
import com.example.androidpart.domain.model.WsMessage
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
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Р”Р’Рђ PreviewView
    val previewView = rememberCameraPreviewView(context)

    LaunchedEffect(Unit) {
        bindCamera(
            context,
            lifecycleOwner,
            previewView,
            targetSize,
            wsClient
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // 50/50 РєРѕРЅС‚РµРЅС‚
        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            CameraEyeView(
                previewView = previewView,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.DarkGray)
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
fun parseStringMatrix(str: String): List<List<Double>> = Json.decodeFromString(str)
fun parseStringList(str: String): List<Double> = Json.decodeFromString(str)

val arJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
    // Это важно для десериализации sealed classes
    classDiscriminator = "type"
}