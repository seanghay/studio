package com.seanghay.studio.gles.egl

import android.graphics.SurfaceTexture
import android.view.Surface
import com.seanghay.studio.gles.egl.EglCore
import com.seanghay.studio.gles.egl.EglSurfaceBase

class EglWindowSurface : EglSurfaceBase {

    private var surface: Surface? = null
    private var releaseSurface = false

    constructor(
        eglCore: EglCore,
        surface: Surface?,
        releaseSurface: Boolean
    ) : super(eglCore) {
        createWindowSurface(surface)
        this.surface = surface
        this.releaseSurface = releaseSurface
    }

    constructor(eglCore: EglCore, surfaceTexture: SurfaceTexture) :
            super(eglCore) {
        createWindowSurface(surfaceTexture)
    }

    fun release() {
        releaseEglSurface()
        if (surface != null) {
            if (releaseSurface) surface?.release()
            surface = null
        }
    }

    fun recreate(newEglCore: EglCore) {
        if (surface == null)
            throw RuntimeException("Not yet implemented for surface texture")

        eglCore = newEglCore
        createWindowSurface(surface)
    }
}