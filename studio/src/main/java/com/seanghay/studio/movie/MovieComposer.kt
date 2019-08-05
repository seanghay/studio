package com.seanghay.studio.movie

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.seanghay.studio.gles.RenderTarget
import com.seanghay.studio.gles.threading.GlHandlerThread

abstract class MovieComposer(protected val context: Context,
                             val timeline: MovieTimeline): LifecycleObserver {

    abstract var renderTarget: RenderTarget

    protected lateinit var renderThread: GlHandlerThread

    val clips get() = timeline.clips

    init {
        registerLifecycle()
    }

    fun post(block: (() -> Unit)) {
        renderThread.post(block)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreate() {
        renderThread = GlHandlerThread()
        renderThread.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        removeLifecycle()
    }

    private fun registerLifecycle() {
        if (context is LifecycleOwner)
            context.lifecycle.addObserver(this)
    }

    private fun removeLifecycle() {
        if (context is LifecycleOwner)
            context.lifecycle.removeObserver(this)

        renderThread.quit()
    }

}