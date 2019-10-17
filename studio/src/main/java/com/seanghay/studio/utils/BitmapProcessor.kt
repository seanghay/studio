/**
 * Designed and developed by Seanghay Yath (@seanghay)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seanghay.studio.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import com.seanghay.studio.utils.BitmapProcessor.CropType.FILL_CENTER
import com.seanghay.studio.utils.BitmapProcessor.CropType.FILL_END
import com.seanghay.studio.utils.BitmapProcessor.CropType.FILL_START
import com.seanghay.studio.utils.BitmapProcessor.CropType.FIT_CENTER
import com.seanghay.studio.utils.BitmapProcessor.CropType.FIT_END
import com.seanghay.studio.utils.BitmapProcessor.CropType.FIT_START
import io.reactivex.Single
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

    fun proceed(): Single<Bitmap> {
        return Single.create {
            it.onSuccess(proceedSync())
        }
    }

    fun proceedSync(): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED

        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val (dstRect: Rect, fill: Boolean) = when (cropType) {
            FIT_CENTER -> fitCenterRect() to false
            FIT_START -> fitStartRect() to false
            FIT_END -> fitEndRect() to false
            FILL_CENTER -> fillCenterRect() to true
            FILL_START -> fillStartRect() to true
            FILL_END -> fillEndRect() to true
        }

        if (!fill) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(.5f)
            val colorFilter = ColorMatrixColorFilter(colorMatrix)
            bgPaint.alpha = 150
            bgPaint.colorFilter = colorFilter

            val bg = FastBlur.blur(source, 14, false)
            canvas.drawBitmap(bg, null, backgroundFillRect(), bgPaint)
        }

        canvas.drawBitmap(source, null, dstRect, null)

        return bitmap
    }

    private fun backgroundFillRect(): Rect {
        val rect = fillCenterRect()
        val scale = 0.1f

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
        return Rect(left, 0, scaledWidth, scaledHeight)
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
        FILL_END;

        fun key(): String {
            return when (this) {
                FIT_CENTER -> "fit-center"
                FIT_START -> "fit-start"
                FIT_END -> "fit-end"
                FILL_CENTER -> "fill-center"
                FILL_START -> "fill-start"
                FILL_END -> "fill-end"
            }
        }

        companion object {
            fun fromKey(key: String): CropType {
                return when (key) {
                    "fit-center" -> FIT_CENTER
                    "fit-start" -> FIT_START
                    "fit-end" -> FIT_END
                    "fill-center" -> FILL_CENTER
                    "fill-start" -> FILL_START
                    "fill-end" -> FILL_END
                    else -> throw RuntimeException()
                }
            }
        }
    }

    companion object {

        fun loadSync(filePath: String): Bitmap {
            val b = BitmapFactory.decodeFile(filePath)
            val exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val rotationInDegrees = exifToDegrees(orientation)
            val matrix = Matrix()
            if (rotationInDegrees != 0) matrix.preRotate(rotationInDegrees.toFloat())
            return Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
        }

        fun load(filePath: String): Single<Bitmap> {
            return Single.create {
                it.onSuccess(loadSync(filePath))
            }
        }

        private fun exifToDegrees(exifOrientation: Int): Int = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
