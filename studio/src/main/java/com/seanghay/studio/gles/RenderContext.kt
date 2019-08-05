package com.seanghay.studio.gles

import android.util.Size

// Renderer
interface RenderContext {
    fun onCreated()
    fun onDraw(): Boolean // SwapBuffer
    fun onSizeChanged(size: Size)
}