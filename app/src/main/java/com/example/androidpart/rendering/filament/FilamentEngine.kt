package com.example.androidpart.rendering.filament

import android.content.Context
import android.util.Log
import android.view.Surface
import com.example.androidpart.domain.ar.PoseMapper
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerPayload
import com.google.android.filament.*
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.Gltfio
import com.google.android.filament.gltfio.MaterialProvider
import com.google.android.filament.gltfio.ResourceLoader
import java.io.File
import java.nio.ByteBuffer

enum class Eye { LEFT, RIGHT }
class FilamentEngine(context: Context) {

    companion object {
        init {
            Filament.init()
            Gltfio.init()
        }

    }

    private val engine = Engine.create()
    private val scene = engine.createScene()
    private val view = engine.createView()
    private val renderer = engine.createRenderer()

    private val cameraEntity = EntityManager.get().create()
    private val camera = engine.createCamera(cameraEntity)

    private val assetLoader = AssetLoader(engine, MaterialProvider(engine), EntityManager.get())
    private val resourceLoader = ResourceLoader(engine)

    private val loadedAssets = mutableMapOf<String, FilamentAsset>()
    private val currentMatrices = mutableMapOf<String, FloatArray>()
    private val lerpFactor = 0.3f
    private val activeModelsInScene = mutableSetOf<String>()

    // Храним две поверхности
    private var leftSwapChain: SwapChain? = null
    private var rightSwapChain: SwapChain? = null


    init {
        view.scene = scene
        view.camera = camera

        renderer.setClearOptions(
            Renderer.ClearOptions().apply {
                clearColor = floatArrayOf(1f, 0f, 0f, 0.3f)
                clear = true
            }
        )

        view.blendMode = View.BlendMode.TRANSLUCENT
        view.isPostProcessingEnabled = false

        scene.skybox = null
        scene.indirectLight = null
        // Камера пока фиксированная (НЕ AR!)
        camera.lookAt(
            0.0, 0.0, 3.0,   // eye
            0.0, 0.0, 0.0,   // center
            0.0, 1.0, 0.0    // up
        )

        // Свет
        val light = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(100_000.0f)
            .direction(0.0f, -1.0f, -1.0f)
            .build(engine, light)

        scene.addEntity(light)
    }
    fun attachSurface(eye: Eye, surface: Surface, width: Int, height: Int) {
        val sc = engine.createSwapChain(surface)
        if (eye == Eye.LEFT) leftSwapChain = sc else rightSwapChain = sc

        // Настраиваем вьюпорт (обычно они одинаковые для обоих глаз)
        view.viewport = Viewport(0, 0, width, height)
        val aspect = width.toDouble() / height.toDouble()
        camera.setProjection(45.0, aspect, 0.1, 100.0, Camera.Fov.VERTICAL)
    }

    fun detachSurface(eye: Eye) {
        if (eye == Eye.LEFT) {
            leftSwapChain?.let { engine.destroySwapChain(it) }
            leftSwapChain = null
        } else {
            rightSwapChain?.let { engine.destroySwapChain(it) }
            rightSwapChain = null
        }
    }

    fun render(frameTimeNanos: Long) {
        // Рендерим левый глаз
        leftSwapChain?.let { sc ->
            if (renderer.beginFrame(sc, frameTimeNanos)) {
                renderer.render(view)
                renderer.endFrame()
            }
        }
        // Рендерим правый глаз (та же вьюха, та же сцена)
        rightSwapChain?.let { sc ->
            if (renderer.beginFrame(sc, frameTimeNanos)) {
                renderer.render(view)
                renderer.endFrame()
            }
        }
    }

    fun loadModel(name: String, file: File) {
        if (loadedAssets.containsKey(name)) return

        val buffer = ByteBuffer.wrap(file.readBytes())
        val asset = assetLoader.createAssetFromBinary(buffer)
            ?: run {
                Log.e("FILAMENT", "Не удалось загрузить модель")
                return
            }

        resourceLoader.loadResources(asset)
        asset.releaseSourceData()

        loadedAssets[name] = asset
        Log.d("FILAMENT_DEBUG", "Загружена модель: $name")
    }

    fun updateMarkersPoses(markers: List<ArMarker>) {
        val detectedInThisFrame = mutableSetOf<String>()

        // 1. Обновляем существующие или добавляем новые
        markers.forEach { marker ->
            val payload = marker.payload
            if (payload is MarkerPayload.Model && marker.tvec != null && marker.rvec != null) {
                val modelName = payload.value.src
                detectedInThisFrame.add(modelName)

                updateSingleMarker(modelName, marker)
            }
        }

        // 2. Очищаем те, что пропали
        cleanupMissingMarkers(detectedInThisFrame)
    }
    private fun updateSingleMarker(modelName: String, marker: ArMarker) {
        val asset = loadedAssets[modelName] ?: return

        // Если модели нет в сцене — добавляем
        if (modelName !in activeModelsInScene) {
            Log.d("FILAMENT_DEBUG", "+++ Добавляем в сцену: $modelName")
            asset.entities.forEach { scene.addEntity(it) }
            activeModelsInScene.add(modelName)
        }

        // Считаем матрицу
        val targetMatrix = PoseMapper.toFilamentMatrix(marker.rvec!!, marker.tvec!!)

        // Коррекция осей и масштаб
        targetMatrix[13] = -targetMatrix[13]
        targetMatrix[14] = -targetMatrix[14]
        val scaleFactor = 10.0f
        android.opengl.Matrix.scaleM(targetMatrix, 0, scaleFactor, scaleFactor, scaleFactor)

        // Применяем LERP
        val finalMatrix = applyLerp(modelName, targetMatrix)

        // Применяем трансформ
        val tm = engine.transformManager
        tm.setTransform(tm.getInstance(asset.root), finalMatrix)
    }

    private fun applyLerp(modelName: String, targetMatrix: FloatArray): FloatArray {
        val current = currentMatrices[modelName] ?: return targetMatrix.also {
            currentMatrices[modelName] = it
        }

        val result = FloatArray(16) { i ->
            current[i] + lerpFactor * (targetMatrix[i] - current[i])
        }
        currentMatrices[modelName] = result
        return result
    }

    private fun cleanupMissingMarkers(detectedInThisFrame: Set<String>) {
        val iterator = activeModelsInScene.iterator()
        while (iterator.hasNext()) {
            val modelName = iterator.next()
            if (modelName !in detectedInThisFrame) {
                Log.d("FILAMENT_DEBUG", "--- Удаляем из сцены: $modelName")

                loadedAssets[modelName]?.entities?.forEach { entity ->
                    scene.removeEntity(entity)
                }

                currentMatrices.remove(modelName) // Сбрасываем LERP
                iterator.remove() // Удаляем из списка активных
            }
        }
    }
}
