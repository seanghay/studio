package com.seanghay.studio.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

fun <R> Bitmap.use(willRecycle: Boolean = true, block: Bitmap.() -> R): R {
    return block().also {
        if (willRecycle) recycle()
    }
}

fun Bitmap.compressQuality(quality: Int): Bitmap {
    return ByteArrayOutputStream().use {
        compress(Bitmap.CompressFormat.JPEG, quality, it)
        val byteArray = it.toByteArray()
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}