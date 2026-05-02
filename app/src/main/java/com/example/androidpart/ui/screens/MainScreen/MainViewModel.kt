package com.example.androidpart.ui.screens.MainScreen

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpart.data.local.SettingsDataStore
import com.example.androidpart.data.remote.WsClient
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerResponse
import com.example.androidpart.domain.model.WsMessage
import com.example.androidpart.rendering.filament.FilamentEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val settings = SettingsDataStore(context)

    // Экземпляр движка живет здесь и не пересоздается при повороте
    val engine: FilamentEngine by lazy { FilamentEngine(context) }

    val wsClient = WsClient()

    // Состояния для UI
    private val _markers = MutableStateFlow<List<ArMarker>>(emptyList())
    val markers: StateFlow<List<ArMarker>> = _markers.asStateFlow()

    private val _currentFrame = MutableStateFlow<Bitmap?>(null)
    val currentFrame: StateFlow<Bitmap?> = _currentFrame.asStateFlow()

    private val arJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    init {
        initNetworkAndData()
    }

    private fun initNetworkAndData() {
        viewModelScope.launch {
            // 1. Подключаемся к сокету
            wsClient.connect { message ->
                try {
                    val response = arJson.decodeFromString<MarkerResponse>(message)
                    _markers.value = response.markers
                } catch (e: Exception) {
                    Log.e("VM_DEBUG", "Parse error: ${e.message}")
                }
            }

            // 2. Получаем калибровку и отправляем INIT
            try {
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
                    wsClient.send(Json.encodeToString(initPacket))
                    Log.d("VM_DEBUG", "Init packet sent successfully")
                }
            } catch (e: Exception) {
                Log.e("VM_DEBUG", "Error during init: ${e.message}")
            }
        }
    }

    // Обновление кадра из CameraX (вызывается из MainScreen)
    fun updateFrame(bitmap: Bitmap) {
        Log.d("UI_DEBUG", "New frame received in UI: ${bitmap.byteCount} bytes")
        _currentFrame.value = bitmap
    }

    // --- УТИЛИТЫ ПАРСИНГА (теперь они не мусорят в UI) ---

    private fun parseStringMatrix(input: String): List<List<Double>> {
        return try {
            input.split(';')
                .filter { it.isNotBlank() }
                .map { row ->
                    row.split(',')
                        .filter { it.isNotBlank() }
                        .map { it.trim().toDouble() }
                }
        } catch (e: Exception) {
            Log.e("VM_DEBUG", "Matrix parse error"); emptyList()
        }
    }

    private fun parseStringList(str: String): List<Double> {
        return try {
            if (str.trim().startsWith("[")) {
                Json.decodeFromString<List<Double>>(str)
            } else {
                str.split(',').filter { it.isNotBlank() }.map { it.trim().toDouble() }
            }
        } catch (e: Exception) {
            Log.e("VM_DEBUG", "List parse error"); emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
        // В идеале тут добавить engine.release() если в FilamentEngine будет такой метод
    }
}