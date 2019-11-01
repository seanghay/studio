package com.seanghay.studioexample.experiment.core

import android.graphics.SurfaceTexture
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glViewport
import android.opengl.GLUtils
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.egl.EglCore
import com.seanghay.studio.gles.egl.EglSurfaceBase
import com.seanghay.studio.gles.egl.EglWindowSurface
import com.seanghay.studio.gles.egl.glScope
import com.seanghay.studio.gles.graphics.FrameBuffer
import com.seanghay.studio.gles.graphics.texture.Texture2d
import com.seanghay.studio.gles.shader.TextureShader
import com.seanghay.studio.utils.BitmapProcessor
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import java.util.LinkedList
import java.util.Queue


data class EditorScene(
    val path: String,
    val texture: Texture2d,
    val width: Int,
    val height: Int,
    val duration: Long = 5000L
)

class EditorEngine private constructor() {

    private var thread: EditorThread? = null
    private var eglCore: EglCore? = null

    private var isThreadStarted = false
    private var isEglConfigured = false
    private var eglSurfaceBase: EglSurfaceBase? = null
    private val frameRateObservable = BehaviorSubject.create<Float>()

    private var viewportWidth = NO_VALUE
    private var viewportHeight = NO_VALUE

    private val frame = FrameConstraint(30.0, frameRateObservable)
    private var clearColor = floatArrayOf(0f, 0f, 0f, 1f)

    private val editorScenes = mutableListOf<EditorScene>()

    private val shader = TextureShader()
    private val frameBufferShader = TextureShader()

    private var displayIndex = 0
    private val frameBuffer = FrameBuffer()

    private val renderer: EditorRenderer = object : EditorRenderer {
        override fun onDrawFrame() {
            try {
                frame.constraint()
                if (frame.delta >= 1.0) {
                    // should update
                    internalUpdates()
                    // should render
                    frame.updates++
                    frame.delta--
                }

                internalDrawFrame()
                frame.logFps()
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                // ignored
            }
        }

        override fun onRequestConfigure() {
            if (!isEglConfigured) {
                if (VERBOSE) Log.d(TAG, "setup EglCore")

                eglCore = EglCore()
                eglCore?.setup()

                // signal that is ready
                thread?.isEglConfigured = true
                isEglConfigured = true
                resetFrame()

                post { onReady() }
            }
        }
    }

    @GlContext
    private fun onReady() {
        if (eglSurfaceBase == null) {
            post(::onReady) // Loop until it's surface
            return
        }

        eglSurfaceBase?.makeCurrent()
        shader.setup()
        shader.setResolution(viewportWidth.toFloat(), viewportHeight.toFloat())
        frameBufferShader.setup()
        frameBufferShader.setResolution(viewportWidth.toFloat(), viewportHeight.toFloat())
        frameBuffer.setup(viewportWidth, viewportHeight)
    }

    fun getFrameRateFlowable(): Flowable<Float> {
        return frameRateObservable.toFlowable(BackpressureStrategy.LATEST)
    }

    private var startedAt = System.currentTimeMillis()

    // Update what needed
    private fun internalUpdates() {
        val now = System.currentTimeMillis()
        if (now - startedAt >= 2000) {
            startedAt += 2000L
            displayIndex++

            if (editorScenes.isNotEmpty()) {
                displayIndex %= editorScenes.size
            }
        }
    }

    private fun resetFrame() {
        frame.reset()
    }

    private val viewMatrix = FloatArray(16)

    private fun configureViewport() {
        if (viewportHeight != NO_VALUE && viewportWidth != NO_VALUE) {
            glViewport(0, 0, viewportWidth, viewportHeight)

            val ratio = viewportWidth.toFloat() / viewportHeight.toFloat()
            val elements = shader.mvpMatrix.elements


//            ProjectionUtils.identities(elements, viewMatrix)
//            ProjectionUtils.applyAspectRatio(elements, ratio)
//            ProjectionUtils.applyLookAt(viewMatrix)
//            ProjectionUtils.times(elements, elements, viewMatrix)


        }
    }


    // mostly OpenGL api calls
    private fun internalDrawFrame() {
        if (eglSurfaceBase == null) return
        eglSurfaceBase?.makeCurrent()

        configureViewport()
        clearColors()

        val frame = editorScenes.getOrNull(0) ?: return

        frameBuffer.use {
            configureViewport()
            clearColors()
            shader.updatePositionAttr()
            shader.draw(frame.texture)
        }

        frameBufferShader.draw(frameBuffer.asTexture())
        eglSurfaceBase?.swapBuffers()
    }

    private fun clearColors() {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3])
    }

    @AnyThread
    private fun post(runnable: (() -> Unit)) {
        thread?.post(runnable)
    }


    @UiThread
    @Synchronized
    fun attachSurfaceTexture(surfaceTexture: SurfaceTexture?) {
        if (surfaceTexture == null) return

        Log.d(TAG, "attach surfaceTexture to EglCore")

        post {
            Log.d(TAG, "attaching surfaceTexture to EglCore")

            if (eglSurfaceBase != null) {
                Log.d(TAG, "releasing old surface")
                eglSurfaceBase?.releaseEglSurface()
            }

            eglSurfaceBase = EglWindowSurface(eglCore!!, surfaceTexture)
            Log.d(TAG, "attached a new window surface")
        }
    }

    @Synchronized
    fun detachSurfaceTexture() {
        if (eglSurfaceBase == null) return

        post {
            Log.d(TAG, "detaching surface")
            eglSurfaceBase?.releaseEglSurface()
            eglSurfaceBase = null
        }
    }

    @AnyThread
    fun setViewportSize(width: Int, height: Int) {
        if (VERBOSE) Log.d(TAG, "configure a new viewport ($width,$height)")
        this.viewportHeight = height
        this.viewportWidth = width

        // set view port to open gl
        signalViewportChange()
    }

    private fun signalViewportChange() {
        if (viewportHeight == NO_VALUE || viewportWidth == NO_VALUE) {
            if (VERBOSE) Log.d(TAG, "Viewport size is invalid, aborting viewport change")
            return
        }

        post {
            glViewport(0, 0, viewportWidth, viewportHeight)
            if (VERBOSE) Log.d(TAG, "viewport has been configured")
        }
    }


    @UiThread
    @Synchronized
    fun initialize() {
        isEglConfigured = false
        thread?.isEglConfigured = false

        if (!isThreadStarted) {
            if (VERBOSE) Log.d(TAG, "create a new thread")
            thread = EditorThread(renderer)
            thread?.start()
            isThreadStarted = true
            if (VERBOSE) Log.d(TAG, "thread has started")
        }
    }

    @UiThread
    fun release() {

        if (VERBOSE) Log.d(TAG, "editor release request")

        post {
            editorScenes.forEach { it.texture.release() }
            shader.release()
            frameBufferShader.release()
        }

        post {
            eglSurfaceBase?.releaseEglSurface()
            eglSurfaceBase = null

            // release EglCore
            eglCore?.release()
            eglCore = null
        }

        // release surface

        post {
            editorEngine = null
        }

        post {
            // Signal stop
            thread?.signalStop()
            thread = null


            isThreadStarted = false
            isEglConfigured = false
        }



        if (VERBOSE) Log.d(TAG, "editor has been released")
    }

    fun attachBitmaps(paths: List<String>) {
        post {
            if (VERBOSE) Log.d(TAG, "Attach bitmaps")
            val bitmaps = paths.associateWith { BitmapProcessor.loadSync(it) }

            val scenes = bitmaps.map {
                glScope("Convert bitmap to texture") {
                    val texture = Texture2d()
                    texture.initialize()
                    texture.use(GL_TEXTURE_2D) {
                        texture.configure(GL_TEXTURE_2D)
                        GLUtils.texImage2D(GL_TEXTURE_2D, 0, it.value, 0)
                        if (VERBOSE) Log.d(TAG, "Bind texture ${texture.id}")
                    }

                    EditorScene(it.key, texture, it.value.width, it.value.height).apply {
                        it.value.recycle()
                    }
                }
            }

            editorScenes.addAll(scenes)
            if (VERBOSE) Log.d(TAG, "Attached bitmaps")

        }

    }


    companion object {
        private const val NO_VALUE = -1
        private const val VERBOSE = true
        private const val TAG = "EditorEngine"
        private var editorEngine: EditorEngine? = null

        @Synchronized
        fun getInstance(): EditorEngine {
            if (editorEngine == null) editorEngine = EditorEngine()
            return editorEngine ?: throw NullPointerException("Editor Engine was null")
        }

        private class FrameConstraint(
            var fps: Double = 60.0,
            var frameRateObservable: BehaviorSubject<Float>
        ) {
            var lastTime = System.nanoTime()
            var delta = 0.0
            var timer = System.currentTimeMillis()
            var updates = 0
            var frames = 0
            val ns = 1000000000.0 / fps

            fun constraint() {
                val now = System.nanoTime()
                delta += (now - lastTime) / ns
                lastTime = now
                frames++
            }

            fun logFps() {
                if (System.currentTimeMillis() - timer > 1000) {
                    timer += 1000
                    frameRateObservable.onNext(frames.toFloat())
                    // Log.d(TAG, "frame constraint: updates $updates, $frames fps")
                    updates = 0
                    frames = 0
                }
            }

            fun reset() {
                lastTime = System.nanoTime()
                delta = 0.0
                timer = System.currentTimeMillis()
                updates = 0
                frames = 0
            }
        }
    }

    interface EditorRenderer {
        fun onDrawFrame()
        fun onRequestConfigure()
    }

    internal class EditorThread(private val renderer: EditorRenderer) : Thread() {

        private val lock = Object()
        private var isRunning = false
        private var isPaused = false

        private val runnableQueues: Queue<Runnable> = LinkedList()
        var isEglConfigured = false

        override fun run() {
            try {
                secureRun()
            } finally {
                synchronized(lock) {
                    if (VERBOSE) Log.d(TAG, "signal the lock")
                    lock.notify()
                }
            }
        }

        private fun secureRun() {
            if (VERBOSE) Log.d(TAG, "thread has started")
            isRunning = true

            while (!isInterrupted) {
                if (!isRunning) break
                if (isPaused) {
                    synchronized(lock) {
                        lock.notifyAll()
                    }

                    continue
                }

                if (isEglConfigured) { // only render when EGL is configured
                    while (runnableQueues.isNotEmpty()) runnableQueues.poll()?.run()
                    renderer.onDrawFrame()
                } else {
                    Log.w(TAG, "EglCore hasn't been configured")
                    renderer.onRequestConfigure()
                    Log.w(TAG, "requested for configured")
                }
            }
        }

        @AnyThread
        fun post(runnable: (() -> Unit)) {
            runnableQueues.add(Runnable(runnable))
        }

        fun signalResume() {
            if (VERBOSE) Log.d(TAG, "signal resume flag to EditorThread")
            synchronized(lock) {
                isPaused = true
                if (VERBOSE) Log.d(TAG, "waiting for signal")
            }
        }

        fun signalPause() {
            if (VERBOSE) Log.d(TAG, "signal pause flag to EditorThread")
            synchronized(lock) {
                isPaused = true
                if (VERBOSE) Log.d(TAG, "waiting for signal")
                lock.wait()
                if (VERBOSE) Log.d(TAG, "received pause the signal")
            }
        }

        fun signalStop() {
            if (VERBOSE) Log.d(TAG, "signal stop flag to EditorThread")
            synchronized(lock) {
                isRunning = false
                if (VERBOSE) Log.d(TAG, "waiting for signal")
                lock.wait()
                if (VERBOSE) Log.d(TAG, "received the signal")
            }
        }

        companion object {
            private const val VERBOSE = true
            private const val TAG = "EditorThread"
        }
    }
}