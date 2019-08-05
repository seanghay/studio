package com.seanghay.studio.gles.threading

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.seanghay.studio.gles.egl.EglCore

class GlHandlerThread : HandlerThread {

    constructor() : super("GlHandlerThread", NORM_PRIORITY)
    constructor(name: String) : super(name)
    constructor(name: String, priority: Int) : super(name, priority)

    private var handler: Handler? = null

    var eglCore: EglCore = EglCore()
        private set

    fun post(runnable: (() -> Unit)) {
        handler?.post {
            runnable()
        }
    }

    @Synchronized
    override fun start() {
        super.start()
        handler = createNewHandler()
        handler?.sendEmptyMessage(MSG_START)
    }

    private fun createNewHandler(): Handler {
        return object : Handler(looper) {
            override fun handleMessage(msg: Message?) {
                super.handleMessage(msg)
                if (msg != null) handleActions(msg)
            }
        }
    }

    private fun handleActions(msg: Message) {
        when (msg.what) {
            MSG_START -> {
                eglCore = EglCore()
                eglCore.setup()
            }
            MSG_STOP -> {
                eglCore.release()
                quitSafely()
            }
        }
    }

    override fun quit(): Boolean {
        handler?.sendEmptyMessage(MSG_STOP) ?: return false
        return true
    }

    override fun quitSafely(): Boolean {
        handler?.sendEmptyMessage(MSG_STOP) ?: return false
        return true
    }

    companion object {
        private const val MSG_START = 1
        private const val MSG_STOP = 2
    }
}
