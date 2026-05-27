package com.example.androidpart.data.local

import android.content.Context

import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.androidpart.core.utils.parseStringList
import com.example.androidpart.core.utils.parseStringMatrix
import com.example.androidpart.domain.model.CameraIntrinsics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_RESOLUTION = stringPreferencesKey("resolution")
        val KEY_FPS = intPreferencesKey("fps")
        val KEY_MARKER_SIZE = floatPreferencesKey("markerSize")
        val KEY_CALIB_RESOLUTION = stringPreferencesKey("calib_resolution")
        val KEY_CAMERA_MATRIX = stringPreferencesKey("camera_matrix")
        val KEY_DIST_COEFFS = stringPreferencesKey("dist_coeffs")
        val KEY_MODELS_HASH = stringPreferencesKey("models_hash")
    }

//    Save
    suspend fun saveModelsHash(hash: String) {
        context.dataStore.edit {
            it[KEY_MODELS_HASH] = hash
        }
    }
    suspend fun saveSettings(resolution: String, fps: Int, markerSize: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_RESOLUTION] = resolution
            preferences[KEY_FPS] = fps
            preferences[KEY_MARKER_SIZE] = markerSize
        }
    }
    suspend fun saveCalibration(cameraMatrix: String, distCoeffs: String, calibRes: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CAMERA_MATRIX] = cameraMatrix
            preferences[KEY_DIST_COEFFS] = distCoeffs
            preferences[KEY_CALIB_RESOLUTION] = calibRes
        }
    }

    // Read
    suspend fun getModelsHash(): String? {
        return context.dataStore.data
            .map { it[KEY_MODELS_HASH] }
            .first()
    }
    val settingsFlow: Flow<Triple<String, Int, Float>> = context.dataStore.data
        .map { preferences ->
            val res = preferences[KEY_RESOLUTION] ?: "640x480"
            val fps = preferences[KEY_FPS] ?: 60
            val markerSize = preferences[KEY_MARKER_SIZE] ?: 0.05f

            Triple(res, fps, markerSize)
        }

    val calibrationFlow: Flow<CameraIntrinsics?> = context.dataStore.data
        .map { preferences ->

            val matrixStr = preferences[KEY_CAMERA_MATRIX]
            val distStr = preferences[KEY_DIST_COEFFS]
            val resStr = preferences[KEY_CALIB_RESOLUTION]

            if (matrixStr == null || distStr == null || resStr == null) return@map null

            val matrix = parseStringMatrix(matrixStr)
            val dist = parseStringList(distStr)

            val (w, h) = resStr.split("x").let {
                it[0].toFloat() to it[1].toFloat()
            }

            CameraIntrinsics(
                cameraMatrix = matrix,
                distCoeffs = dist,
                calibWidth = w,
                calibHeight = h
            )
        }
}
