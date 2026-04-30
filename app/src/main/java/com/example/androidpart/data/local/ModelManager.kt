package com.example.androidpart.data.local

import android.content.Context
import android.util.Log
import com.example.androidpart.data.remote.DetectorRepository
import java.io.File

class ModelManager(
    private val repository: DetectorRepository,
    private val context: Context,
    private val settings: SettingsDataStore
) {
    private val modelsDir = File(context.filesDir, "models")

    suspend fun prepareModels(): Boolean {
        return try {

            Log.d("MODEL", "=== START MODEL SYNC ===")

            val dictResponse = repository.getDictionaries().getOrNull() ?: return false

            val serverHash = dictResponse.modelsHash
            val localHash = settings.getModelsHash()

            Log.d("MODEL", "Server hash: $serverHash")
            Log.d("MODEL", "Local hash: $localHash")

            if (serverHash == localHash) {
                Log.d("MODEL", "HASH совпадает — ничего не качаем")
                return true
            }

            Log.d("MODEL", "HASH изменился — начинаем синк")
            clearModels()

            Log.d("MODEL", "Словарь: ${dictResponse.currentDict}")

            val models = repository.getModels(dictResponse.currentDict).getOrNull() ?: return false

            Log.d("MODEL", "Модели с сервера: ${models.size}")

            if (!modelsDir.exists()) modelsDir.mkdirs()

            // 🔥 4. скачиваем ВСЕ заново (важно при hash-стратегии)
            models.forEach { filename ->

                Log.d("MODEL", "СКАЧИВАЕМ: $filename")

                val body = repository.downloadModel(filename)
                    .getOrNull() ?: return false

                val file = File(modelsDir, filename)

                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("MODEL", "СОХРАНЕНО: $filename")
            }

            settings.saveModelsHash(serverHash)
            Log.d("MODEL", "HASH обновлён: $serverHash")
            Log.d("MODEL", "=== SYNC DONE ===")
            true
        } catch (e: Exception) {
            Log.e("MODEL", "SYNC ERROR", e)
            false
        }
    }
    private fun clearModels() {
        Log.d("MODEL", "Очищаем старые модели")

        modelsDir.listFiles()?.forEach {
            it.delete()
        }
    }
}