package com.seanghay.studio.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class BitmapDiskCache(val context: Context) {

    fun set(key: String, value: Bitmap) {
        val dir = File(context.cacheDir, "bmp-cache")
        val file = File(dir, "bitmap-cache-$key.jpg")
        val fileOutputStream = FileOutputStream(file)
        value.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    }

    fun contains(key: String): Boolean {
        val file = File(context.cacheDir, "bitmap-cache-$key.jpg")
        return file.exists()
    }

    fun get(key: String): Bitmap? {
        return if (contains(key)) {
            val file = File(context.cacheDir, "bitmap-cache-$key.jpg")
            BitmapFactory.decodeFile(file.path)
        } else null
    }

    fun getOrElse(key: String, block: () -> Bitmap): Bitmap {
        return get(key) ?: block()
    }

    fun clear() {
        val dir = File(context.cacheDir, "bmp-cache")
        if (dir.exists()) deleteFolder(dir)
    }

    private fun deleteFolder(folder: File) {
        val files = folder.listFiles()
        if (files != null) { //some JVMs return null for empty dirs
            for (f in files) {
                if (f.isDirectory) {
                    deleteFolder(f)
                } else {
                    f.delete()
                }
            }
        }
        folder.delete()
    }

}