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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

class BitmapDiskCache(val context: Context) {

    fun set(key: String, value: Bitmap) {
        val dir = File(context.cacheDir, "bmp-cache")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "bitmap-cache-$key.jpg")
        val fileOutputStream = FileOutputStream(file)
        value.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    }

    fun contains(key: String): Boolean {
        val dir = File(context.cacheDir, "bmp-cache")
        val file = File(dir, "bitmap-cache-$key.jpg")
        return file.exists()
    }

    fun get(key: String): Bitmap? {
        val dir = File(context.cacheDir, "bmp-cache")
        return if (contains(key)) {
            val file = File(dir, "bitmap-cache-$key.jpg")
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
        if (files != null) { // some JVMs return null for empty dirs
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
