package com.seanghay.studioexample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.opengl.Matrix
import com.seanghay.studio.gles.graphics.mat4
import com.seanghay.studio.gles.graphics.texture.Texture2d
import com.seanghay.studio.gles.transition.*
import java.io.File

class ImageRender(
    var files: List<File>,
    var width: Int = -1,
    var height: Int = -1
) {

    private var textureShader = TransitionalTextureShader(DirectionalwarpTransition())

    private val transitions = arrayOf(
        BurnTransition(),
        LuminanceMeltTransition(),
        PerlinTransition(),
        PolarFunctionTransition(),
        RotateScaleFadeTransition(),
        SwapTransition(),
        CrosswarpTransition(),
        ColorphaseTransition(),
        CircleCropTransition(),
        DirectionalTransition(),
        FadecolorTransition(),
        FadegrayscaleTransition(),
        KaleidoscopeTransition(),
        MorphTransition(),
        PinwheelTransition(),
        SwapTransition(),
        WipeRightTransition()
    )


    private val shaders = transitions.map { TransitionalTextureShader(it).also { v ->
        v.isFlipVertical = true
        v.isFlipHorizontal = true
    } }
    private val images = files.filter { it.name.endsWith(".jpg") }
    private val textures = images.mapIndexed { index, file ->

        Texture2d().also {
            it.initialize()
            it.use(GL_TEXTURE_2D) {
                it.configure(GL_TEXTURE_2D)
                GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmapOf(file), 0)
            }
        }
    }

    private var currentSlide = 0
    
    private val viewProjectMatrix =  FloatArray(16)

    init {
        textureShader.setResolution(width.toFloat(), height.toFloat())
        textureShader.setup()

        shaders.forEach {
            it.setResolution(width.toFloat(), height.toFloat())
            it.setup()
        }

        val ratio = (width.toFloat() / height.toFloat())
        val projectionMatrix = FloatArray(16)
        val viewMatrix = FloatArray(16)

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(viewProjectMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

    }

    private fun bitmapOf(file: File): Bitmap {
        val b = BitmapFactory.decodeFile(file.path)
        val rotationInDegrees = exifToDegrees(Companion.getRotationOf(file.path))
        val matrix = android.graphics.Matrix()
        if (rotationInDegrees != 0) {
            matrix.preRotate(rotationInDegrees.toFloat())
        }

        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
    }



    private fun swapTexture() {
        textureShader = shaders.random()
        currentSlide++
        textureShader.mvpMatrix.elements = viewProjectMatrix
    }

    private fun next(): Texture2d {
        return textures[(currentSlide + 1) % images.size]
    }

    private fun current(): Texture2d {
        return textures[currentSlide % images.size]
    }

    fun renderAt(frameIndex: Int) {
        if (frameIndex % 100 == 0) {
            swapTexture()
        }
        textureShader.progress = (frameIndex % 100).toFloat() / 100f
        glClear(GL_COLOR_BUFFER_BIT)
        glClearColor(0f, 0f, 0f, 1f)
        glViewport(0, 0, width, height)
        textureShader.draw(current(), next())
    }


    private fun exifToDegrees(exifOrientation: Int): Int = when (exifOrientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }

    companion object {
        private fun getRotationOf(path: String): Int {
            val exifInterface = ExifInterface(path)
            return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }
    }
}