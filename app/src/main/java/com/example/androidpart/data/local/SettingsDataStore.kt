package com.example.androidpart.data.local

import android.content.Context

import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_RESOLUTION = stringPreferencesKey("resolution")
        val KEY_FPS = intPreferencesKey("fps")
        val KEY_MARKER_SIZE = floatPreferencesKey("markerSize")
    }

    // Сохраняем данные
    suspend fun saveSettings(resolution: String, fps: Int, markerSize: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_RESOLUTION] = resolution
            preferences[KEY_FPS] = fps
            preferences[KEY_MARKER_SIZE] = markerSize
        }
    }

    // Читаем данные
    val settingsFlow: Flow<Triple<String, Int, Float>> = context.dataStore.data
        .map { preferences ->
            val res = preferences[KEY_RESOLUTION] ?: "1080x1200"
            val fps = preferences[KEY_FPS] ?: 60
            val markerSize = preferences[KEY_MARKER_SIZE] ?: 0.05f

            Triple(res, fps, markerSize)
        }
    }