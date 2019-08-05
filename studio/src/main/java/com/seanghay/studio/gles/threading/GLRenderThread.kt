package com.seanghay.studio.gles.threading

import com.seanghay.studio.gles.RenderContext
import com.seanghay.studio.gles.RenderTarget
import com.seanghay.studio.gles.egl.EglCore
import java.util.*

class GLRenderThread(val renderContext: RenderContext): Thread() {

    val eglCore: EglCore = EglCore()

    var renderTarget: RenderTarget? = null

    private var isRunning = false
    private var isSizeChanged = false
    private val queueEvents: Queue<Runnable> = LinkedList()

    fun post(runnable: (() -> Unit)) {
        queueEvents.add(Runnable(runnable))
    }

    override fun run() {
        val target = renderTarget ?: throw RuntimeException("Target was null")
        isRunning = true
        eglCore.setup()
        target.initialize(eglCore)
        target.makeCurrent()
        renderContext.onCreated()
        val viewport = target.getViewport()

        renderContext.onSizeChanged(viewport)

        while (!interrupted() && isRunning) {
            target.makeCurrent()

            invokeSizeChanged(target)
            while (queueEvents.isNotEmpty()) queueEvents.poll()?.run()

            if (renderContext.onDraw())
                target.swapBuffers()

        }

        target.release()
        eglCore.release()
    }

    private fun invokeSizeChanged(target: RenderTarget) {
        if (isSizeChanged) {
            renderContext.onSizeChanged(target.getViewport())
            isSizeChanged = false
        }
    }

    @Synchronized
    fun quit() {
        isRunning = false
    }

    @Synchronized
    fun release() {
        renderTarget?.release()
        eglCore.release()
    }

}