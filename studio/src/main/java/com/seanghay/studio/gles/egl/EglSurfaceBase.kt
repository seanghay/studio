package com.seanghay.studio.gles.egl

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


open class EglSurfaceBase(protected var eglCore: EglCore) {

    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var width: Int = -1
    private var height: Int = -1

    private fun checkRationals() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) fail("surface already created")
    }

    open fun createWindowSurface(surface: Any?) {
        checkRationals()
        eglSurface = eglCore.createWindowSurface(surface)
    }

    open fun createOffscreenSurface(width: Int, height: Int) {
        checkRationals()
        eglSurface = eglCore.createOffscreenSurface(width, height)
        this.width = width
        this.height = height
    }

    fun width(): Int {
        return if (width < 0) eglCore.querySurface(eglSurface, EGL14.EGL_WIDTH)
        else width
    }


    fun height(): Int {
        return if (height < 0) eglCore.querySurface(eglSurface, EGL14.EGL_HEIGHT)
        else height
    }

    open fun releaseEglSurface() {
        eglCore.releaseSurface(eglSurface)
        eglSurface = EGL14.EGL_NO_SURFACE
        width = -1
        height = -1
    }

    open fun makeCurrent() {
        eglCore.makeCurrent(eglSurface)
    }

    open fun makeCurrentReadFrom(readSurface: EglSurfaceBase) {
        eglCore.makeCurrent(eglSurface, readSurface.eglSurface)
    }

    open fun swapBuffers(): Boolean {
        return eglCore.swapBuffers(eglSurface).also {
            if (!it)
                Log.d(TAG, "WARNING: swapBuffers() failed")
        }
    }

    open fun setPresentationTime(nsecs: Long) {
        eglCore.setPresentationTime(eglSurface, nsecs)
    }

    @Throws(IOException::class)
    fun saveFrame(file: File) {
        if (!eglCore.isCurrent(eglSurface)) fail("Expected EGL Context/Surface is not current")
        val filename = file.toString()
        val width = width()
        val height = height()
        val buffer = ByteBuffer.allocate(width * height * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        glScope {
            GLES20.glReadPixels(
                0, 0, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer
            )
        }

        buffer.rewind()

        BufferedOutputStream(FileOutputStream(filename)).use {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                copyPixelsFromBuffer(buffer)
                compress(Bitmap.CompressFormat.PNG, 90, it)
                recycle()
            }
        }

        Log.d(TAG, "Saved " + width + "x" + height + " frame as '" + filename + "'")
    }

    companion object {
        private const val TAG = "EglSurfaceBase"

        private fun fail(message: String): Nothing {
            throw RuntimeException("EglSurfaceBase: $message")
        }
    }
}