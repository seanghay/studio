package com.seanghay.studioexample

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.view.TextureView
import com.seanghay.studio.core.StudioDrawable
import com.seanghay.studio.core.StudioRenderThread



class VideoComposer(private val context: Context): StudioDrawable {

    private var studioRenderThread: StudioRenderThread? = null
    private var width: Int = -1
    private var height: Int = -1

    var shade: Float = 0f


    val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(
            surfaceTexture: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            this@VideoComposer.width = width
            this@VideoComposer.height= height

            studioRenderThread?.let {
                it.height = height
                it.width = width
            }

        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture?) {

        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture?): Boolean {
            return false
        }

        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            if (surfaceTexture == null) return

            this@VideoComposer.width = width
            this@VideoComposer.height= height

            studioRenderThread = StudioRenderThread(surfaceTexture).also {
                it.height = height
                it.width = width
                it.drawable = this@VideoComposer
                it.start()
            }

        }
    }


    override fun onDraw(): Boolean {
        if (width != -1 && height != -1) {
            GLES20.glViewport(0, 0, width, height)
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearColor(1f - shade, 1f, 0f, 1f)

        return true
    }



    fun release() {
        studioRenderThread?.quit()
    }
}