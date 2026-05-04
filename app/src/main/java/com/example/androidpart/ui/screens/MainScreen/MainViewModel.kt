package com.example.androidpart.ui.screens.MainScreen

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpart.core.math.parseStringList
import com.example.androidpart.core.math.parseStringMatrix
import com.example.androidpart.data.local.SettingsDataStore
import com.example.androidpart.data.remote.WsClient
import com.example.androidpart.data.remote.arJson
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
import kotlinx.serialization.json.jsonObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val settingsDataStore = SettingsDataStore(context)

    // Экземпляр движка живет здесь и не пересоздается при повороте
    val engine: FilamentEngine by lazy { FilamentEngine(context) }

    val wsClient = WsClient()

    // Состояния для UI
    private val _markers = MutableStateFlow<List<ArMarker>>(emptyList())
    val markers: StateFlow<List<ArMarker>> = _markers.asStateFlow()

    private val _currentFrame = MutableStateFlow<Bitmap?>(null)
    val currentFrame: StateFlow<Bitmap?> = _currentFrame.asStateFlow()


    init {
        initNetworkAndData()
    }

    private fun initNetworkAndData() {
        viewModelScope.launch {
            // 1. Подключаемся к сокету
            wsClient.connect { message ->
                try {
                    val jsonElement = arJson.parseToJsonElement(message).jsonObject
                    // Сначала проверяем, ЧТО пришло
// Сначала проверяем, ЧТО пришло
                    if (jsonElement.containsKey("markers")) {
                        val response = arJson.decodeFromString<MarkerResponse>(message)
                        _markers.value = response.markers
                    } else if (jsonElement.containsKey("error")) {
                        Log.e("VM_DEBUG", "Server error message: ${jsonElement["error"]}")
                    } else {
                        Log.d("VM_DEBUG", "Other message: $message")
                    }

                } catch (e: Exception) {
                    Log.e("VM_DEBUG", "Parse error: ${e.message}")
                }
            }

            // 2. Получаем калибровку и отправляем INIT
            try {
                val calibration = settingsDataStore.calibrationFlow.first()
                val currentSettings = settingsDataStore.settingsFlow.first()
                val markerSize = currentSettings.third

                if (calibration != null) {
                    val initPacket: WsMessage = WsMessage.Init(
                        camera_matrix = calibration.cameraMatrix,
                        dist_coeffs = calibration.distCoeffs,
                        marker_length = markerSize.toDouble()

                    )
                    val jsonToSend = arJson.encodeToString<WsMessage>(initPacket)

                    wsClient.send(jsonToSend)

                }
            } catch (e: Exception) {
                Log.e("VM_DEBUG", "Error during init: ${e.message}")
            }
        }
    }

    // Обновление кадра из CameraX (вызывается из MainScreen)
    fun updateFrame(bitmap: Bitmap) {
        _currentFrame.value = bitmap
    }

    // --- УТИЛИТЫ ПАРСИНГА (теперь они не мусорят в UI) ---



    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
        // В идеале тут добавить engine.release() если в FilamentEngine будет такой метод
    }
}