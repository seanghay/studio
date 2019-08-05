package com.seanghay.studio.gles.egl

class EglOffscreenSurface(
    eglCore: EglCore, width: Int,
    height: Int
) : EglSurfaceBase(eglCore) {

    init {
        createOffscreenSurface(width, height)
    }

    fun release() {
        releaseEglSurface()
    }
}