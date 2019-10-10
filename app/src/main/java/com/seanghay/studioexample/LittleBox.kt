package com.seanghay.studioexample

import android.graphics.SurfaceTexture
import android.opengl.GLES10.*
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.seanghay.studio.core.Studio
import com.seanghay.studio.gles.RenderContext
import com.seanghay.studio.utils.clamp
import java.util.*

class LittleBox(
    val activity: AppCompatActivity,
    val surfaceTexture: SurfaceTexture,
    var width: Int,
    var height: Int
) {

    var playProgress: (Float) -> Unit = {}
    var isPlaying = false
        private set

    private var composer: VideoComposer? = null
    private val onReadyQueue: Queue<Runnable> = LinkedList()
    private lateinit var display: Studio.OutputSurface

    private val studio = Studio.create(activity) {
        display = createOutputSurface()
        display.fromSurfaceTexture(surfaceTexture)
        setOutputSurface(display)
        setSize(Size(width, height))
        while (onReadyQueue.isNotEmpty()) onReadyQueue.poll()?.run()
    }


    fun getStudio() = studio

    fun setComposer(videoComposer: VideoComposer) {
        this.composer = videoComposer
        this.composer?.width = width
        this.composer?.height = height

        onReadyQueue.add(Runnable {
            studio.setRenderContext(videoComposer)
            studio.dispatchDraw()
            studio.dispatchDraw()
        })
    }

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        studio.setSize(Size(width, height))
    }

    fun release() {
        studio.release()
    }

    fun draw() {
        studio.dispatchDraw()
    }

    private fun startPlay() {

        if (isPlaying) return
        var lastTime = System.nanoTime()
        var delta = 0.0
        val ns = 1000000000.0 / 60.0
        var timer = System.currentTimeMillis()
        var updates = 0
        var frames = 0

        val totalDuration = composer?.totalDuration ?: 0L
        var startedAt = timer

        isPlaying = true

        val offset = composer?.progress ?: 0f

        while (isPlaying) {
            val elapsed = System.currentTimeMillis() - startedAt
            var progress = (elapsed.toFloat() / totalDuration.toFloat())
            if (progress >= 1.0) startedAt = System.currentTimeMillis()
            progress = progress.clamp(0f, 1f)

            val now = System.nanoTime()

            delta += (now - lastTime) / ns
            lastTime = now

            if (delta >= 1.0) {
                // Set progress
                composer?.progress = offset + progress
                updates++
                delta--
                playProgress(offset + progress)
            }

            studio.draw()

            frames++
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000
                Log.d("LittleBox", "$updates ups, $frames fps")
                updates = 0
                frames = 0
            }
        }
    }

    fun play() {
        studio.post { startPlay() }
    }

    fun stop() {
        isPlaying = false
    }

    fun pause() {
        isPlaying = false
    }


    fun exportToVideo(
        path: String,
        audioPath: String?,
        progress: ((Float) -> Unit) = {},
        done: () -> Unit = {}
    ) {

        val composer = composer ?: return
        isPlaying = false

        val mp4Composer = Mp4Composer(studio, composer, path, composer.totalDuration) {
            done()
            studio.setOutputSurface(display)
        }

        mp4Composer.audioPath = audioPath
        mp4Composer.onProgressChange = progress
        mp4Composer.width = composer.width
        mp4Composer.height = composer.height
        mp4Composer.create()

        studio.post {
            mp4Composer.start()
        }
    }

    companion object LittleContext : RenderContext {
        private const val TAG = "LittleContext"

        override fun onCreated() {
            Log.d(TAG, "onCreated")
        }

        override fun onDraw(): Boolean {
            Log.d(TAG, "draw")
            glClearColor(1f, 0f, 1f, 1f)
            glClear(GL_COLOR_BUFFER_BIT)
            return true
        }

        override fun onSizeChanged(size: Size) {
            Log.d(TAG, "onResize")
            glViewport(0, 0, size.width, size.height)
        }
    }
}

