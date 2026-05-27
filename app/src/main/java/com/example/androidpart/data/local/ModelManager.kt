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

    suspend fun prepareModels(
        onProgress: (Int) -> Unit = {}
    ): Boolean {
        return try {


            val dictResponse = repository.getDictionaries().getOrNull() ?: return false

            val serverHash = dictResponse.modelsHash
            val localHash = settings.getModelsHash()


            if (serverHash == localHash) {
                return true
            }


            clearModels()


            val models = repository.getModels(dictResponse.currentDict).getOrNull() ?: return false


            if (!modelsDir.exists()) modelsDir.mkdirs()

            val total = models.size
            var current = 0

            // 🔥 4. скачиваем ВСЕ заново (важно при hash-стратегии)
            models.forEach { filename ->


                val body = repository.downloadModel(filename)
                    .getOrNull() ?: return false

                val file = File(modelsDir, filename)

                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                current++
                val percent = if (total == 0) 100 else (current * 100) / total

                onProgress(percent)
            }

            settings.saveModelsHash(serverHash)
            true
        } catch (e: Exception) {
            false
        }
    }
    private fun clearModels() {

        modelsDir.listFiles()?.forEach {
            it.delete()
        }
    }
}