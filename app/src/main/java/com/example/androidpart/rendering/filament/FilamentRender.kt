package com.example.androidpart.rendering.filament

import android.content.Context
import android.view.SurfaceView
import com.google.android.filament.*
import android.os.SystemClock

class FilamentRenderer(
    private val context: Context
) {

    private val engine: Engine = Engine.create()
    private val scene: Scene = engine.createScene()
    private val view: View = engine.createView()
    private val renderer: Renderer = engine.createRenderer()
    private val camera: Camera = engine.createCamera()

    private val surfaceView: SurfaceView = SurfaceView(context)

    private var swapChain: SwapChain? = null

    init {
        setupView()
        setupCamera()
        setupSurface()
    }

    // -----------------------
    // VIEW
    // -----------------------
    private fun setupView() {
        view.scene = scene
        view.camera = camera

        // 1.6.0 way
        view.setPostProcessingEnabled(true)

    }

    // -----------------------
    // CAMERA
    // -----------------------
    private fun setupCamera() {
        camera.setProjection(
            45.0,
            1.0,
            0.1,
            100.0,
            Camera.Fov.VERTICAL
        )

        camera.lookAt(
            0.0, 0.0, 3.0,
            0.0, 0.0, 0.0,
            0.0, 1.0, 0.0
        )
    }

    // -----------------------
    // SURFACE
    // -----------------------
    private fun setupSurface() {
        surfaceView.holder.addCallback(object : android.view.SurfaceHolder.Callback {

            override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                swapChain = engine.createSwapChain(holder.surface)
            }

            override fun surfaceChanged(
                holder: android.view.SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                view.viewport = Viewport(0, 0, width, height)
            }

            override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                swapChain?.let {
                    engine.destroySwapChain(it)
                }
                swapChain = null
            }
        })
    }

    // -----------------------
    // RENDER LOOP (1.6.0 FIXED)
    // -----------------------
    fun render() {
        val sc = swapChain ?: return

        val time = SystemClock.elapsedRealtimeNanos()

        if (renderer.beginFrame(sc, time)) {
            renderer.render(view)
            renderer.endFrame()
        }
    }

    // -----------------------
    // API
    // -----------------------
    fun getView(): SurfaceView = surfaceView
    fun getScene(): Scene = scene
    fun getEngine(): Engine = engine
    fun getCamera(): Camera = camera
}