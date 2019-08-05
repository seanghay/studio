package com.seanghay.studio.gles.egl

import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import android.view.Surface


class EglCore(
    private val sharedContext: EGLContext = EGL14.EGL_NO_CONTEXT,
    private val flags: Int = 0
) {

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglConfig: EGLConfig? = null
    private var glVersion = -1

    fun setup() {
        rationalChecks()
        getDisplay()
        initializeEgl()
        tryGles3Context()
        tryFallbackContext()
        Log.d(TAG, "EGLContext created, client version: ${getClientVersion()}")
        logCurrent("Setup completed")
    }

    private fun getClientVersion(): Int {
        val values = IntArray(1)
        EGL14.eglQueryContext(eglDisplay, eglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0)
        return values[0]
    }

    private fun tryFallbackContext() {
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            val config = getConfig(flags, 2) ?: fail("Unable to find a suitable EGLConfig")
            val attribute2List = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            val context = runs {
                EGL14.eglCreateContext(eglDisplay, config, sharedContext, attribute2List, 0)
            }

            eglConfig = config
            eglContext = context
            glVersion = 2
        }
    }

    private fun tryGles3Context() {
        if (flags and FLAG_GLES3 != 0) {
            getConfig(flags, 3)?.let { config ->
                val attribute3List = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
                )

                val context = EGL14.eglCreateContext(eglDisplay, config, sharedContext, attribute3List, 0)

                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    eglConfig = config
                    eglContext = context
                    glVersion = 3
                }
            }
        }
    }

    private fun initializeEgl() {
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            eglDisplay = EGL14.EGL_NO_DISPLAY
            fail("unable to initialize EGL14")
        }
    }

    private fun getDisplay() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) fail("unable to get EGL14 display")

    }

    private fun rationalChecks() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) fail("EGL display already setup!")
    }


    fun getConfig(flags: Int, version: Int): EGLConfig? {
        val renderableType = if (version >= 3) EGLExt.EGL_OPENGL_ES3_BIT_KHR
        else EGL14.EGL_OPENGL_ES2_BIT

        val attributesList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            //EGL14.EGL_DEPTH_SIZE, 16,
            //EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
            EGL14.EGL_NONE
        )

        if (flags and FLAG_RECORDABLE != 0) {
            attributesList[attributesList.size - 3] =
                EGL_RECORDABLE_ANDROID
            attributesList[attributesList.size - 2] = 1
        }

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)

        if (!EGL14.eglChooseConfig(
                eglDisplay, attributesList, 0, configs,
                0, configs.size, numConfigs, 0
            )
        ) {
            Log.w(TAG, "unable to find RGB8888 / $version EGLConfig")
            return null
        }

        return configs[0]
    }

    fun release() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(eglDisplay)
        }

        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglConfig = null
    }

    protected fun finalize() {
        try {
            if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
                Log.w(TAG, "WARNING: EglCore was not explicitly released -- state may be leaked")
                release()
            }
        } catch (err: Exception) {
            err.printStackTrace()
            fail("Error while finalized by GC")
        }
    }

    fun releaseSurface(eglSurface: EGLSurface) {
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
    }


    fun createWindowSurface(surface: Any?): EGLSurface {
        if (surface !is Surface && surface !is SurfaceTexture) {
            fail("Invalid surface: $surface")
        }

        val surfaceAttributes = intArrayOf(
            EGL14.EGL_NONE
        )

        return runs("eglCreateWindowSurface") {
            EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttributes, 0)
                ?: fail("Surface was null")
        }
    }


    fun createOffscreenSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttributes = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE
        )

        return runs("Create offscreen surface") {
            EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttributes, 0)
                ?: fail("Surface was null")
        }
    }

    fun makeCurrent(eglSurface: EGLSurface) {
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "NOTE: Make current without display")
        }

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            fail("eglMakeCurrent failed")
        }
    }

    fun makeCurrent(drawSurface: EGLSurface, readSurface: EGLSurface) {
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) Log.d(TAG, "NOTE: Make current without display")
        if (!EGL14.eglMakeCurrent(eglDisplay, drawSurface, readSurface, eglContext))
            fail("eglMakeCurrent failed")
    }

    fun destroyCurrent() {
        if (!EGL14.eglMakeCurrent(
                eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
        ) {
            fail("eglMakeCurrent failed")
        }
    }

    fun swapBuffers(eglSurface: EGLSurface): Boolean {
        return EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    fun setPresentationTime(eglSurface: EGLSurface, nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, nsecs)
    }


    fun isCurrent(eglSurface: EGLSurface): Boolean {
        return eglContext == EGL14.eglGetCurrentContext()
                && eglSurface == EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
    }

    fun querySurface(eglSurface: EGLSurface, name: Int): Int {
        val value = IntArray(1)
        EGL14.eglQuerySurface(eglDisplay, eglSurface, name, value, 0)
        return value[0]
    }

    fun queryString(name: Int): String {
        return EGL14.eglQueryString(eglDisplay, name)
    }

    fun getGlesVersion(): Int {
        return glVersion
    }

    companion object {
        private const val TAG = "EglCore"
        const val FLAG_RECORDABLE = 0x01
        const val FLAG_GLES3 = 0x02
        const val EGL_RECORDABLE_ANDROID = 0x3142

        fun <T> runs(
            message: String? = "",
            block: (() -> T)
        ): T {
            return block().also {
                val error = EGL14.eglGetError()
                if (error != EGL14.EGL_SUCCESS) {
                    throw RuntimeException("$message: EGL error 0x${error.toString(16)}")
                }
            }
        }

        fun fail(message: String): Nothing {
            throw RuntimeException("EglCore: $message")
        }

        fun logCurrent(message: String) {
            val display = EGL14.eglGetCurrentDisplay()
            val context = EGL14.eglGetCurrentContext()
            val surface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)

            Log.i(
                TAG, "Current EGL (" + message + "): display=" + display + ", context=" + context +
                        ", surface=" + surface
            )
        }
    }
}