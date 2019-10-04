package com.seanghay.studio.core

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import androidx.annotation.MainThread

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.seanghay.studio.gles.RenderContext
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.egl.EglCore
import com.seanghay.studio.gles.egl.EglSurfaceBase


class Studio private constructor() : DefaultLifecycleObserver {

    private var eglCore: EglCore? = null
    private var thread: StudioThread = StudioThread()
    private var handler: Handler? = null
    private var outputSurface: OutputSurface? = null
    private var isStarted = false
    private var renderContext: RenderContext? = null
    private var size = Size(-1, -1)
    private var onReadyCallback: (Studio.(EglCore) -> Unit)? = null

    override fun onCreate(owner: LifecycleOwner) {
        initialize()
    }

    override fun onPause(owner: LifecycleOwner) {
        // Pause rendering
        // Queue pause action to handler thread
    }

    override fun onResume(owner: LifecycleOwner) {
        // Continue rendering
        // Queue resume action to handler thread
    }

    override fun onDestroy(owner: LifecycleOwner) {
        release()
    }

    fun initialize() {
        thread.start()
        handler = Handler(thread.looper)
        isStarted = true
        initializeEglCore()
    }

    @MainThread
    fun setRenderContext(renderContext: RenderContext) {
        this.renderContext = renderContext
        dispatchUpdate()
    }

    @MainThread
    fun setSize(size: Size) {
        this.size = size
        dispatchSizeUpdate()
    }

    @MainThread
    private fun dispatchUpdate() {
        renderContext?.let { post(true, it::onCreated) }
    }

    @MainThread
    private fun dispatchSizeUpdate() {
        renderContext?.let {
            post {
                if (!isValidSize() && outputSurface != null) {
                    val surfaceWidth = outputSurface?.eglSurface?.width() ?: -1
                    val surfaceHeight = outputSurface?.eglSurface?.height() ?: -1
                    size = Size(surfaceWidth, surfaceHeight)
                }

                it.onSizeChanged(size)
            }
        }
    }

    fun setOutputSurface(outputSurface: OutputSurface) {
        this.outputSurface = outputSurface
        post { outputSurface.makeCurrent() }
        dispatchSizeUpdate()
    }

    @MainThread
    fun release() {
        post {
            eglCore?.release()
            eglCore = null
            thread.quitSafely()
            isStarted = false
        }
    }

    @MainThread
    fun createOutputSurface(): OutputSurface {
        val eglSurfaceBase = EglSurfaceBase(requireEglCore())
        return OutputSurface(eglSurfaceBase, requireNotNull(handler))
    }

    @MainThread
    fun dispatchDraw() {
        post {
            draw()
        }
    }


    fun directDraw(runnable: () -> Unit, afterDraw: () -> Unit) {
        val output = outputSurface ?: return
        output.makeCurrent()
        runnable()
        renderContext?.onDraw()
        output.swapBuffers()
    }


    @GlContext
    private fun draw() {
        val output = outputSurface ?: return
        output.makeCurrent()
        renderContext?.onDraw()
        output.swapBuffers()
    }

    fun post(check: Boolean = true, runnable: () -> Unit) {
        handler?.post {
            if (check) nullChecks()
            runnable()
        }
    }

    private fun initializeEglCore() {
        post(false) {
            eglCore = EglCore()
            eglCore?.setup()
            dispatchOnReady()
        }
    }

    private fun dispatchOnReady() {
        onReadyCallback?.invoke(this, requireEglCore())
    }

    private fun isValidSize(): Boolean = size.height > 0 && size.width > 0

    private fun nullChecks() {
        checkNotNull(eglCore) { "EglCore was null, please setup or create a new one" }
        checkNotNull(handler) { "Handler was null, please setup or create a new one" }
        check(isStarted) { "Thread hasn't started yet!" }
    }

    private fun requireEglCore(): EglCore = requireNotNull(eglCore)

    companion object {
        @MainThread
        @JvmStatic
        fun create(lifecycleOwner: LifecycleOwner, onReady: Studio.(EglCore) -> Unit = {}): Studio {
            val studio = Studio()
            studio.onReadyCallback = onReady
            lifecycleOwner.lifecycle.addObserver(studio)
            return studio
        }
    }

    class OutputSurface internal constructor(
        val eglSurface: EglSurfaceBase,
        val handler: Handler
    ) {

        fun makeCurrent() {
            eglSurface.makeCurrent()
        }

        fun swapBuffers() {
            eglSurface.swapBuffers()
        }

        /**
         * Can be called if the surface is released
         */
        fun fromSurfaceTexture(surfaceTexture: SurfaceTexture) {
            handler.post {
                eglSurface.createWindowSurface(surfaceTexture)
            }
        }

        fun release() {
            handler.post {
                eglSurface.releaseEglSurface()
            }
        }

        fun fromSurface(surface: Surface) {
            handler.post {
                eglSurface.createWindowSurface(surface)
            }
        }
    }

    class StudioThread : HandlerThread("StudioThread")
}