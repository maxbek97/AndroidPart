package com.example.androidpart.rendering.filament

import android.content.Context
import android.util.Log
import android.view.Choreographer
import android.view.SurfaceView
import android.view.Surface
import com.google.android.filament.*
import com.google.android.filament.android.UiHelper
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
    private class EyeResources(val view: View, val camera: Camera, val cameraEntity: Entity)

    // Храним две поверхности
    private var leftSwapChain: SwapChain? = null
    private var rightSwapChain: SwapChain? = null

    private var swapChain: SwapChain? = null
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

        scene.addEntities(asset.entities)

        loadedAssets[name] = asset
    }
    fun placeModelInFront(name: String) {
        val asset = loadedAssets[name] ?: return

        val tm = engine.transformManager
        val instance = tm.getInstance(asset.root)

        val matrix = FloatArray(16)

        android.opengl.Matrix.setIdentityM(matrix, 0)
        android.opengl.Matrix.translateM(matrix, 0, 0f, 0f, -2f)

        tm.setTransform(instance, matrix)
    }
}
