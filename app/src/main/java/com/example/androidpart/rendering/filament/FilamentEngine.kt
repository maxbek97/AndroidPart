package com.example.androidpart.rendering.filament

import android.content.Context
import android.util.Log
import android.view.Surface
import com.example.androidpart.domain.ar.PoseMapper
import com.example.androidpart.domain.model.ArMarker
import com.example.androidpart.domain.model.MarkerPayload
import com.example.androidpart.domain.model.ModelData
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

    private val localTransformCache = mutableMapOf<String, FloatArray>()

    private var leftSwapChain: SwapChain? = null
    private var rightSwapChain: SwapChain? = null


    init {
        view.scene = scene
        view.camera = camera

        renderer.setClearOptions(
            Renderer.ClearOptions().apply {
                clearColor = floatArrayOf(0f, 0f, 0f, 0f)
                clear = true
            }
        )

        view.blendMode = View.BlendMode.TRANSLUCENT
        view.isPostProcessingEnabled = false

        scene.skybox = null
        scene.indirectLight = null
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
    fun updateCameraProjection(
        cameraMatrix: List<List<Double>>,
        frameWidth: Float,  // Ширина реально пришедшего Bitmap
        frameHeight: Float, // Высота реально пришедшего Bitmap
        calibWidth: Float,  // Ширина, для которой делалась калибровка
        calibHeight: Float  // Высота, для которой делалась калибровка
    ) {
        // Вычисляем коэффициенты масштабирования, если Bitmap отличается от калибровки
        val scaleX = frameWidth / calibWidth
        val scaleY = frameHeight / calibHeight

        Log.d("AR_CENTER_DEBUG", """
        Frame Size: ${frameWidth}x${frameHeight}
        Calib Size: ${calibWidth}x${calibHeight}
        Scale: ${scaleX}x${scaleY}
    """.trimIndent())

        // Извлекаем параметры из матрицы камеры (OpenCV format)
        val fx = cameraMatrix[0][0] * scaleX
        val fy = cameraMatrix[1][1] * scaleY
        val cx = cameraMatrix[0][2] * scaleX
        val cy = cameraMatrix[1][2] * scaleY

// ЛОГИ ДЛЯ ПРОВЕРКИ СМЕЩЕНИЯ
        Log.d("AR_CENTER_DEBUG", """
        Expected Center: ${frameWidth / 2} x ${frameHeight / 2}
        Actual CX/CY: $cx x $cy
        Diff X: ${cx - frameWidth / 2}
        Diff Y: ${cy - frameHeight / 2}
    """.trimIndent())

        val near = 0.1
        val far = 100.0

        // Собираем матрицу проекции для Filament (Column-major)
        // Используем формулу для перевода OpenCV матрицы в OpenGL-совместимую
        val projectionMatrix = DoubleArray(16).apply {
            this[0] = 2.0 * fx / frameWidth
            this[5] = 2.0 * fy / frameHeight
            this[8] = (2.0 * cx / frameWidth) - 1.0
            this[9] = 1.0 - (2.0 * cy / frameHeight)
            this[10] = -(far + near) / (far - near)
            this[11] = -1.0
            this[14] = -(2.0 * far * near) / (far - near)
            this[15] = 0.0
        }

        camera.setCustomProjection(projectionMatrix, near, far)
    }
    fun attachSurface(eye: Eye, surface: Surface, width: Int, height: Int) {
        val sc = engine.createSwapChain(surface)
        if (eye == Eye.LEFT) leftSwapChain = sc else rightSwapChain = sc

        // Настраиваем вьюпорт (обычно они одинаковые для обоих глаз)
        view.viewport = Viewport(0, 0, width, height)
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
                return
            }

        resourceLoader.loadResources(asset)
        asset.releaseSourceData()

        loadedAssets[name] = asset
    }

    fun updateMarkersPoses(markers: List<ArMarker>) {
        val detectedInThisFrame = mutableSetOf<String>()

        // 1. Обновляем существующие или добавляем новые
        markers.forEach { marker ->
            val payload = marker.payload
            if (payload is MarkerPayload.Model && marker.tvec != null && marker.rvec != null) {
                val modelName = payload.value.src
                detectedInThisFrame.add(modelName)

                updateSingleMarker(marker)
            }
        }

        // 2. Очищаем те, что пропали
        cleanupMissingMarkers(detectedInThisFrame)
    }
    private fun updateSingleMarker(marker: ArMarker) {
        val payload = marker.payload as? MarkerPayload.Model ?: return
        val modelData = payload.value
        val modelName = modelData.src

        val asset = loadedAssets[modelName] ?: return

        // Если модели нет в сцене — добавляем
        if (modelName !in activeModelsInScene) {
            Log.d("FILAMENT_DEBUG", "+++ Добавляем в сцену: $modelName")
            asset.entities.forEach { scene.addEntity(it) }
            activeModelsInScene.add(modelName)
        }

        // Считаем матрицу
        val markerMatrix = PoseMapper.toFilamentMatrix(marker.rvec!!, marker.tvec!!)
        markerMatrix[13] = -markerMatrix[13] // Инверсия Y (OpenCV -> Filament)
        markerMatrix[14] = -markerMatrix[14]

        Log.d("AR_POSE_DEBUG", "TVEC: ${marker.tvec}, RVEC: ${marker.rvec}")
        val localMatrix = getLocalTransform(modelData)
        val targetMatrix = FloatArray(16)
        android.opengl.Matrix.multiplyMM(targetMatrix, 0, markerMatrix, 0, localMatrix, 0)
        //android.opengl.Matrix.rotateM(targetMatrix, 0, 270f, 1f, 0f, 0f)


        //val scaleFactor = 0.7f
//        android.opengl.Matrix.scaleM(targetMatrix, 0, scaleFactor, scaleFactor, scaleFactor)
//        android.opengl.Matrix.translateM(targetMatrix, 0, 0f, 0f, -0.5f)
        // Применяем LERP
        val finalMatrix = applyLerp(modelName, targetMatrix)
        val tm = engine.transformManager
        tm.setTransform(tm.getInstance(asset.root), finalMatrix)
    }
    private fun getLocalTransform(modelData: ModelData): FloatArray {
        // Используем src как ключ, так как для одного файла настройки всегда одинаковые
        return localTransformCache.getOrPut(modelData.src) {
            val localMatrix = FloatArray(16)
            android.opengl.Matrix.setIdentityM(localMatrix, 0)

            // Порядок в Unity: Translate -> Rotate -> Scale
            // В OpenGL (Matrix.multiply) он будет обратным при умножении,
            // но эти методы модифицируют текущую матрицу последовательно:

            // 1. Смещение
            android.opengl.Matrix.translateM(
                localMatrix, 0,
                modelData.start_position.getOrElse(0) { 0f },
                modelData.start_position.getOrElse(1) { 0f },
                modelData.start_position.getOrElse(2) { 0f }
            )

            // 2. Вращение
            val rx = modelData.rotation.getOrElse(0) { 0f }
            val ry = modelData.rotation.getOrElse(1) { 0f }
            val rz = modelData.rotation.getOrElse(2) { 0f }
            if (rx != 0f) android.opengl.Matrix.rotateM(localMatrix, 0, rx, 1f, 0f, 0f)
            if (ry != 0f) android.opengl.Matrix.rotateM(localMatrix, 0, ry, 0f, 1f, 0f)
            if (rz != 0f) android.opengl.Matrix.rotateM(localMatrix, 0, rz, 0f, 0f, 1f)

            // 3. Масштаб
            android.opengl.Matrix.scaleM(
                localMatrix, 0,
                modelData.scale.getOrElse(0) { 1f },
                modelData.scale.getOrElse(1) { 1f },
                modelData.scale.getOrElse(2) { 1f }
            )

            localMatrix
        }
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
