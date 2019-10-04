package com.seanghay.studio.core

import com.seanghay.studio.gles.RenderContext

interface StudioDrawable: RenderContext {
    fun onSetup()
    fun renderAtProgress(progress: Float)

    override fun onCreated() {
        onSetup()
    }

}

