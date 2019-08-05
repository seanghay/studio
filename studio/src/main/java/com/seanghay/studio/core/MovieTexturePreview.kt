package com.seanghay.studio.core

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.TextureView
import com.seanghay.studio.gles.RenderTarget
import com.seanghay.studio.gles.egl.EglCore
import com.seanghay.studio.gles.egl.EglWindowSurface
import com.seanghay.studio.gles.annotation.GlContext

class MovieTexturePreview : TextureView, RenderTarget {

    private var windowSurface: EglWindowSurface? = null

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int): super(context, attributeSet, defStyle)

    @GlContext
    override fun initialize(eglCore: EglCore) {
        windowSurface = EglWindowSurface(eglCore, surfaceTexture)
    }

    override fun swapBuffers() {
        windowSurface?.swapBuffers()
    }

    override fun makeCurrent() {
        windowSurface?.makeCurrent()
    }

    override fun release() {
        windowSurface?.release()
    }

    override fun context(): Context {
        return context
    }

    override fun getViewport(): Size {
        return Size(width, height)
    }


}