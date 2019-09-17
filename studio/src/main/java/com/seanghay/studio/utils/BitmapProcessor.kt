package com.seanghay.studio.utils

import android.graphics.*
import android.media.ExifInterface
import java.io.IOException

class BitmapProcessor(private val source: Bitmap) {

    private val width: Int = source.width
    private val height: Int = source.height

    private var scaledWidth: Int = width
    private var scaledHeight: Int = height


    @Throws(IOException::class)
    fun processToFile(path: String) {

    }

    fun quality(quality: Int) {

    }

    fun cropType(cropType: CropType) {

    }

    fun crop(width: Int, height: Int) {
        this.scaledWidth = width
        this.scaledHeight = height
    }

    fun proceed(): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED
        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)

        val sourceRatio = width.toFloat() / height.toFloat()
        val targetRatio = scaledWidth.toFloat() / scaledHeight.toFloat()

        val canvas = Canvas(bitmap)
        val srcRect = Rect(0, 0, width, height)

        var left = (scaledWidth - width) / 2
        var right = left + width
        var top = (scaledHeight - height) / 2
        var bottom = top + height


        val dstRect = Rect(left, top, right, bottom)
        canvas.drawBitmap(source, srcRect, dstRect, null)


        return bitmap
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