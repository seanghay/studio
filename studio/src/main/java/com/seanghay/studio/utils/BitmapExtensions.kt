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
