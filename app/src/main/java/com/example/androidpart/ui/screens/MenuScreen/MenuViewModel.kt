package com.example.androidpart.ui.screens.MenuScreen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidpart.data.local.ModelManager
import kotlinx.coroutines.launch

class MenuViewModel(
    private val modelManager: ModelManager
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun onStartClicked(onSuccess: () -> Unit) {
        Log.d("MODEL", "onStartClicked вызван")
        viewModelScope.launch {
            isLoading = true
            error = null

            Log.d("MODEL", "Начинаем prepareModels()")
            val result = modelManager.prepareModels()

            isLoading = false

            if (result) {
                onSuccess()
            } else {
                error = "Ошибка загрузки моделей"
            }
        }
    }
}