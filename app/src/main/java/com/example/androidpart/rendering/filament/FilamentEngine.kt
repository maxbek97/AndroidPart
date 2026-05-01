package com.example.androidpart.rendering.filament

import android.content.Context
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

class FilamentEngine(context: Context) {

    companion object {
        init {
            // Загружаем нативные библиотеки самого Filament и gltfio
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

    // Инструменты для загрузки GLB
    private val assetLoader = AssetLoader(engine, MaterialProvider(engine), EntityManager.get())
    private val resourceLoader = ResourceLoader(engine)

    // Храним загруженные ассеты (модели)
    private val loadedAssets = mutableMapOf<String, FilamentAsset>()
    // Исправлено: объявляем переменную явно
    private var uiHelper: UiHelper? = null
    private var swapChain: SwapChain? = null

    fun setup(surfaceView: SurfaceView) {
        view.scene = scene
        view.camera = camera

        // Свет (без него модель будет черной)
        val light = EntityManager.get().create()

        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(100000.0f)
            .direction(0.0f, -1.0f, -1.0f)
            .build(engine, light)
        scene.addEntity(light)

        surfaceView.setZOrderOnTop(true)
        surfaceView.holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)

        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.CHECK).apply {
            renderCallback = object : UiHelper.RendererCallback {
                override fun onNativeWindowChanged(surface: Surface) {
                    swapChain?.let { engine.destroySwapChain(it) }
                    swapChain = engine.createSwapChain(surface)
                }

                override fun onDetachedFromSurface() {
                    swapChain?.let { engine.destroySwapChain(it) }
                    swapChain = null
                }

                override fun onResized(width: Int, height: Int) {
                    view.viewport = Viewport(0, 0, width, height)
                }
            }
            attachTo(surfaceView)
        }
    }

    private fun startRenderLoop() {
        val frameScheduler = android.view.Choreographer.getInstance()
        val runRender = object : android.view.Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                swapChain?.let { sc ->
                    if (renderer.beginFrame(sc, frameTimeNanos)) {
                        renderer.render(view)
                        renderer.endFrame()
                    }
                }
                frameScheduler.postFrameCallback(this)
            }
        }
        frameScheduler.postFrameCallback(runRender)
    }

    fun updateModel(modelName: String, matrix: FloatArray, modelFile: File) {
        val asset = loadedAssets.getOrPut(modelName) {
            loadGlbFile(modelFile) ?: return
        }

        val tm = engine.transformManager
        val instance = tm.getInstance(asset.root)
        tm.setTransform(instance, matrix)
    }

    private fun loadGlbFile(file: File): FilamentAsset? {
        if (!file.exists()) return null

        val buffer = ByteBuffer.wrap(file.readBytes())
        // Исправлено: в Kotlin gltfio метод называется createAsset
        val asset = assetLoader.createAssetFromBinary(buffer) ?: return null

        resourceLoader.loadResources(asset)

        // Важно: вызываем обновление трансформаций после загрузки ресурсов
        asset.releaseSourceData()

        scene.addEntities(asset.entities)
        return asset
    }
}