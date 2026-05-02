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

//class FilamentEngine(context: Context) {
//
//    companion object {
//        init {
//            // Загружаем нативные библиотеки самого Filament и gltfio
//            Filament.init()
//            Gltfio.init()
//        }
//    }
//    private val engine = Engine.create()
//    private val scene = engine.createScene()
//    private val view = engine.createView()
//    private val renderer = engine.createRenderer()
//
//    private val cameraEntity = EntityManager.get().create()
//    private val camera = engine.createCamera(cameraEntity)
//
//    // Инструменты для загрузки GLB
//    private val assetLoader = AssetLoader(engine, MaterialProvider(engine), EntityManager.get())
//    private val resourceLoader = ResourceLoader(engine)
//
//    // Храним загруженные ассеты (модели)
//    private val loadedAssets = mutableMapOf<String, FilamentAsset>()
//    // Исправлено: объявляем переменную явно
//    private var uiHelper: UiHelper? = null
//    private var swapChain: SwapChain? = null
//
//    fun setup(surfaceView: SurfaceView) {
//        Log.d("FILAMENT_DEBUG", "Setup started")
//        view.scene = scene
//        view.camera = camera
//
//        scene.indirectLight = null
//        scene.skybox = null // Это критично!
//
//        view.blendMode = View.BlendMode.TRANSLUCENT
//        view.isPostProcessingEnabled = false
//
//        renderer.setClearOptions(Renderer.ClearOptions().apply {
//            clearColor = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f) // RGBA, где A=0
//            clear = true
//        })
//
//        // Свет (без него модель будет черной)
//        val light = EntityManager.get().create()
//
//        LightManager.Builder(LightManager.Type.DIRECTIONAL)
//            .color(1.0f, 1.0f, 1.0f)
//            .intensity(100000.0f)
//            .direction(0.0f, -1.0f, -1.0f)
//            .build(engine, light)
//        scene.addEntity(light)
//
//        surfaceView.setZOrderOnTop(true)
//        surfaceView.holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)
//
//        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.CHECK).apply {
//            renderCallback = object : UiHelper.RendererCallback {
//                override fun onNativeWindowChanged(surface: Surface) {
//                    Log.d("FILAMENT_DEBUG", "Surface changed/created")
//                    swapChain?.let { engine.destroySwapChain(it) }
//                    swapChain = engine.createSwapChain(surface)
//                }
//
//                override fun onDetachedFromSurface() {
//                    Log.d("FILAMENT_DEBUG", "Surface detached")
//                    swapChain?.let { engine.destroySwapChain(it) }
//                    swapChain = null
//                }
//
//                override fun onResized(width: Int, height: Int) {
//                    Log.d("FILAMENT_DEBUG", "Resized to $width x $height")
//                    view.viewport = Viewport(0, 0, width, height)
//
//                    val aspect = width.toDouble() / height.toDouble()
//                    camera.setProjection(45.0, aspect, 0.1, 100.0, Camera.Fov.VERTICAL)
//                }
//            }
//            attachTo(surfaceView)
//        }
//        startRenderLoop()
//    }
//    private var frameReported = false
//    private fun startRenderLoop() {
//        val frameScheduler = android.view.Choreographer.getInstance()
//        val runRender = object : android.view.Choreographer.FrameCallback {
//            override fun doFrame(frameTimeNanos: Long) {
//                swapChain?.let { sc ->
//                    renderer.setClearOptions(Renderer.ClearOptions().apply {
//                        clearColor = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
//                        clear = true
//                    })
//                    if (renderer.beginFrame(sc, frameTimeNanos)) {
//                        if (!frameReported) {
//                            Log.d("FILAMENT_DEBUG", "First frame rendered successfully!")
//                            frameReported = true
//                        }
//                        renderer.render(view)
//                        renderer.endFrame()
//                    }
//                }
//                frameScheduler.postFrameCallback(this)
//            }
//        }
//        frameScheduler.postFrameCallback(runRender)
//    }
//
//    fun updateModel(modelName: String, matrix: FloatArray, modelFile: File) {
//        val asset = loadedAssets.getOrPut(modelName) {
//            loadGlbFile(modelFile) ?: return
//        }
//
//        val tm = engine.transformManager
//        val instance = tm.getInstance(asset.root)
//        tm.setTransform(instance, matrix)
//    }
//
//    private fun loadGlbFile(file: File): FilamentAsset? {
//        if (!file.exists()) return null
//
//        val buffer = ByteBuffer.wrap(file.readBytes())
//        // Исправлено: в Kotlin gltfio метод называется createAsset
//        val asset = assetLoader.createAssetFromBinary(buffer) ?: return null
//
//        resourceLoader.loadResources(asset)
//
//        // Важно: вызываем обновление трансформаций после загрузки ресурсов
//        asset.releaseSourceData()
//
//        scene.addEntities(asset.entities)
//        return asset
//    }
//}

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

    private var swapChain: SwapChain? = null

    init {
        view.scene = scene
        view.camera = camera

        // Камера пока фиксированная (НЕ AR!)
        camera.lookAt(
            0.0, 0.0, 3.0,   // eye
            0.0, 0.0, 0.0,   // center
            0.0, 1.0, 0.0    // up
        )

        camera.setProjection(
            45.0,
            1.0,
            0.1,
            100.0,
            Camera.Fov.VERTICAL
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
    fun attachSurface(surface: Surface, width: Int, height: Int) {
        swapChain = engine.createSwapChain(surface)

        view.viewport = Viewport(0, 0, width, height)

        val aspect = width.toDouble() / height.toDouble()
        camera.setProjection(45.0, aspect, 0.1, 100.0, Camera.Fov.VERTICAL)

        startRenderLoop()
    }

    fun detachSurface() {
        swapChain?.let { engine.destroySwapChain(it) }
        swapChain = null
    }
    private fun startRenderLoop() {
        val choreographer = Choreographer.getInstance()

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {

                swapChain?.let { sc ->
                    if (renderer.beginFrame(sc, frameTimeNanos)) {
                        renderer.render(view)
                        renderer.endFrame()
                    }
                }

                choreographer.postFrameCallback(this)
            }
        }

        choreographer.postFrameCallback(callback)
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