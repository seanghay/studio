package com.seanghay.studioexample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.ExifInterface.*
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.util.SparseArray
import android.util.SparseIntArray
import androidx.core.util.set
import com.seanghay.studio.gles.graphics.Matrix4f
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


    private val shaders = transitions.map { TransitionalTextureShader(it).also { t -> t.isFlipVertical = true } }
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



    init {
        textureShader.setResolution(width.toFloat(), height.toFloat())
        textureShader.setup()

        shaders.forEach {
            it.setResolution(width.toFloat(), height.toFloat())
            it.setup()
        }
    }

    private fun bitmapOf(file: File): Bitmap {
        val b =  BitmapFactory.decodeFile(file.path)
        val rotationInDegrees = exifToDegrees(getRotationOf(file.path))
        val matrix = Matrix()
        if (rotationInDegrees != 0) {
            matrix.preRotate(rotationInDegrees.toFloat())
        }

        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
    }

    private fun swapTexture() {
        textureShader = shaders.random()
        currentSlide++

//        val currentIndex = currentSlide % images.size
//        // val nextIndex = (currentSlide + 1) % images.size
//
//        val matrix = mat4()
//        textureShader.mvpMatrix = matrix
//
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


    fun getRotationOf(path: String): Int {
        val exifInterface = ExifInterface(path)
        return exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }
}