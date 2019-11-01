package com.seanghay.studioexample

import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MyRenderer : GLSurfaceView.Renderer {

    var lastTime = System.nanoTime()
    var delta = 0.0
    var ns = 1000000000.0 / 60.0
    var timer = System.currentTimeMillis()
    var updates = 0
    var frames = 0


    override fun onDrawFrame(gl: GL10?) {
        glClearColor(0f, 0f, 0f, 1f)
        glClear(GL_COLOR_BUFFER_BIT)

        val now = System.nanoTime()
        delta += (now - lastTime) / ns
        lastTime = now
        if (delta >= 1.0) {

            updates++
            delta--
        }
        frames++

        if (System.currentTimeMillis() - timer > 1000) {
            timer += 1000
            Log.d("RenderActivity", "$updates ups, $frames fps")
            updates = 0
            frames = 0
        }

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

    }

}


class RenderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val renderer = MyRenderer()
        val surfaceView = GLSurfaceView(this)
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setRenderer(renderer)
        setContentView(surfaceView)
    }
}