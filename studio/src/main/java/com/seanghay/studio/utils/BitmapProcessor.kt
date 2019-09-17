package com.seanghay.studio.utils

import android.graphics.*
import android.media.ExifInterface
import androidx.core.graphics.plus
import com.seanghay.studio.utils.BitmapProcessor.CropType.*
import java.io.IOException

class BitmapProcessor(private val source: Bitmap) {

    private val width: Int = source.width
    private val height: Int = source.height

    private var scaledWidth: Int = width
    private var scaledHeight: Int = height
    private var cropType: CropType = FIT_CENTER


    @Throws(IOException::class)
    fun processToFile(path: String) {

    }

    fun quality(quality: Int) {

    }

    fun cropType(cropType: CropType) {
        this.cropType = cropType
    }

    fun crop(width: Int, height: Int) {
        this.scaledWidth = width
        this.scaledHeight = height
    }

    fun proceed(): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED

        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val (dstRect: Rect, fill: Boolean) =  when(cropType) {
            FIT_CENTER -> fitCenterRect() to false
            FIT_START -> fitStartRect() to false
            FIT_END -> fitEndRect() to false
            FILL_CENTER -> fillCenterRect() to true
            FILL_START -> fillStartRect() to true
            FILL_END -> fillEndRect() to true
        }

        if (!fill) {
            val  bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(.4f)
            val colorFilter = ColorMatrixColorFilter(colorMatrix)
            bgPaint.alpha = 100
            bgPaint.colorFilter = colorFilter
            canvas.drawBitmap(source, null, backgroundFillRect(), bgPaint)
        }

        canvas.drawBitmap(source, null, dstRect, null)

        return bitmap
    }

    private fun backgroundFillRect(): Rect {
        val rect = fillCenterRect()
        val scale = 0.25f

        val d = ((rect.right - rect.left) * scale).toInt()
        val h = ((rect.bottom - rect.top) * scale).toInt()

        rect.left -= d
        rect.right += d

        rect.top -= h
        rect.bottom += h

        return rect
    }


    private fun fillEndRect(): Rect {
        val ratio = width.toFloat() / height.toFloat()
        val bottom = (scaledWidth.toFloat() / ratio).toInt()
        val top = scaledHeight - bottom
        return Rect(0, top, scaledWidth, scaledHeight)
    }

    private fun fillStartRect(): Rect {
        val ratio = width.toFloat() / height.toFloat()
        val bottom = (scaledWidth.toFloat() / ratio).toInt()
        val top = 0
        return Rect(0, top, scaledWidth, bottom + top)
    }

    private fun fillCenterRect(): Rect {
        val ratio = width.toFloat() / height.toFloat()
        val bottom = (scaledWidth.toFloat() / ratio).toInt()
        val top = ((scaledHeight - bottom) / 2f).toInt()
        return Rect(0, top, scaledWidth, bottom + top)
    }

    private fun fitEndRect(): Rect {
        val ratio = width.toFloat() / height.toFloat()
        val right = (scaledHeight * ratio).toInt()
        val left = scaledWidth - right
        return Rect(left , 0, scaledWidth, scaledHeight)
    }

    private fun fitCenterRect(): Rect {
        val ratio = width.toFloat() / height.toFloat()
        val right = (scaledHeight * ratio).toInt()
        val left = if (scaledWidth > right) ((scaledWidth - right) / 2f).toInt()
        else ((right - scaledWidth) / 2f).toInt()
        return Rect(left, 0, right + left, scaledHeight)
    }


    private fun fitStartRect(): Rect {
        val ratio = width.toFloat() / height.toFloat()
        val right = (scaledHeight * ratio).toInt()
        return Rect(0, 0, right, scaledHeight)
    }

    enum class CropType {
        FIT_CENTER,
        FIT_START,
        FIT_END,
        FILL_CENTER,
        FILL_START,
        FILL_END
    }


    companion object {
        fun load(filePath: String): Bitmap {
            val b = BitmapFactory.decodeFile(filePath)
            val exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotationInDegrees = exifToDegrees(orientation)
            val matrix = Matrix()
            if (rotationInDegrees != 0) matrix.preRotate(rotationInDegrees.toFloat())
            return Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
        }

        private fun exifToDegrees(exifOrientation: Int): Int = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}