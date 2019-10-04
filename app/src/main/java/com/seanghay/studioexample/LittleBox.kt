package com.seanghay.studioexample

import android.graphics.SurfaceTexture
import android.opengl.GLES10.*
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.seanghay.studio.core.Studio
import com.seanghay.studio.gles.RenderContext
import java.io.File
import java.util.*

class LittleBox(
    val activity: AppCompatActivity,
    val surfaceTexture: SurfaceTexture,
    var width: Int,
    var height: Int
) {

    private var composer: VideoComposer? = null
    private val onReadyQueue: Queue<Runnable> = LinkedList()
    private lateinit var display: Studio.OutputSurface

    private val studio = Studio.create(activity) {
        display = createOutputSurface()
        display.fromSurfaceTexture(surfaceTexture)

        setOutputSurface(display)
        setSize(Size(width, height))

        while(onReadyQueue.isNotEmpty()) onReadyQueue.poll()?.run()
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


    fun exportToVideo(path: String) {
        val composer = composer ?: return

        val mp4Composer = Mp4Composer(studio, composer, path,  composer.totalDuration) {
            studio.setOutputSurface(display)
        }

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

