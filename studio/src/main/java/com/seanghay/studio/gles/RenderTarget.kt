package com.seanghay.studio.gles

import android.content.Context
import android.util.Size
import com.seanghay.studio.gles.egl.EglCore

// Previews
interface RenderTarget {
    fun initialize(eglCore: EglCore)
    fun swapBuffers()
    fun makeCurrent()
    fun release()
    fun context(): Context
    fun getViewport(): Size
}
