package com.seanghay.studio.core

import android.graphics.SurfaceTexture
import android.opengl.GLES20.*
import android.util.Size
import android.view.TextureView
import com.seanghay.studio.gles.RenderContext


class StudioEngine: TextureView.SurfaceTextureListener {

    private var viewportWidth: Int = 0
    private var viewportHeight: Int = 0
    private lateinit var renderThread: StudioRenderThread

    private fun setViewport(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
    }

    private fun onSurfaceCreated(surface: SurfaceTexture, width: Int, height: Int) {
        setViewport(width, height)
        renderThread = StudioRenderThread(surface)
        renderThread.start()
    }

    private fun onSurfaceSizeChanged(width: Int, height: Int) {
        setViewport(width, height)
    }


    private fun releaseSurface() {
        renderThread.quit()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        onSurfaceSizeChanged(width, height)
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        releaseSurface()

        return false
    }


    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if (surface == null) return
        onSurfaceCreated(surface, width, height)
    }



}