package com.seanghay.studio.core

import android.graphics.SurfaceTexture
import android.util.Log
import com.seanghay.studio.gles.RenderContext
import com.seanghay.studio.gles.egl.EglCore
import com.seanghay.studio.gles.egl.EglWindowSurface

class StudioRenderThread(private val surfaceTexture: SurfaceTexture): Thread() {

    private val eglCore = EglCore()
    private lateinit var windowSurface: EglWindowSurface
    private var isRunning = true

    private fun setup() {
        eglCore.setup()
        windowSurface = EglWindowSurface(eglCore, surfaceTexture)
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
        return true
    }

    override fun run() {
        setup()
        while (isRunning && !interrupted()) {
            makeCurrent()
            if (drawFrame())
                swapBuffers()
        }
        release()
    }

    fun quit() {
        isRunning = false
    }

}