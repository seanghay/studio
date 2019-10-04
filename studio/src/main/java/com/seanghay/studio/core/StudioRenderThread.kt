package com.seanghay.studio.core

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.util.Log
import com.seanghay.studio.gles.RenderContext
import com.seanghay.studio.gles.egl.EglCore
import com.seanghay.studio.gles.egl.EglWindowSurface


class StudioRenderThread(private val surfaceTexture: SurfaceTexture): Thread() {

    private val eglCore = EglCore()
    private lateinit var windowSurface: EglWindowSurface
    private var isRunning = true
    var drawable: StudioDrawable? = null

    var height: Int = -1
    var width: Int  = -1

    private fun setup() {
        eglCore.setup()
        windowSurface = EglWindowSurface(eglCore, surfaceTexture)
    }

    fun recreate() {
        windowSurface.recreate(eglCore)
    }

    private fun swapBuffers() {
        windowSurface.swapBuffers()
    }

    private fun makeCurrent() {
        windowSurface.makeCurrent()
    }

    private fun release() {
        windowSurface.release()
        eglCore.release()
    }

    private fun drawFrame(): Boolean {
        return drawable?.onDraw() ?: false
    }

    override fun run() {
        setup()
        makeCurrent()
        drawable?.onSetup()

        while (isRunning && !interrupted()) {
            makeCurrent()
            if (drawFrame())
                swapBuffers()
        }

        release()
    }

    fun getEglCore(): EglCore = eglCore

    fun quit() {
        isRunning = false
    }

}