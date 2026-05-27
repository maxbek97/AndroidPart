package com.example.androidpart.ui.screens.MenuScreen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpart.data.local.ModelManager
import com.example.androidpart.data.local.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MenuViewModel(
    private val modelManager: ModelManager,
    private val settings: SettingsDataStore
) : ViewModel() {

    var progress by mutableStateOf(0)
        private set
    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set
    fun clearError() {
        error = null
    }
    fun onStartClicked(onSuccess: () -> Unit) {
        viewModelScope.launch {

            val calibration = settings.calibrationFlow.first()

            val hasCalibration = calibration != null &&
            calibration.cameraMatrix.isNotEmpty() &&
                    calibration.distCoeffs.isNotEmpty()

            if (!hasCalibration) {
                error = "Сначала откалибруйте камеру"
                return@launch
            }

            isLoading = true
            error = null
            progress = 0

            val result = modelManager.prepareModels {
                percent -> progress = percent
            }

            isLoading = false

            if (result) {
                onSuccess()
            } else {
                error = "Ошибка загрузки моделей"
            }
        }
    }
}