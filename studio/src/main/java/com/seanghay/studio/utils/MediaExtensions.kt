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

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Size
import java.nio.ByteBuffer

inline fun <R> String.toMetadataRetriever(block: MediaMetadataRetriever.() -> R): R {
    return MediaMetadataRetriever()
        .also { it.setDataSource(this) }
        .use(block)
}

inline fun <R> MediaMetadataRetriever.use(block: MediaMetadataRetriever.() -> R): R {
    return block(this).also {
        release()
    }
}

fun MediaMetadataRetriever.mimeType(): String {
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
}

fun MediaMetadataRetriever.durationMillis(): Long {
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
}

fun MediaMetadataRetriever.durationUs(): Long {
    return durationMillis() * 1000L
}

fun MediaMetadataRetriever.durationSeconds(): Long {
    return durationMillis() / 1000L
}

fun MediaMetadataRetriever.videoHeight(): Int {
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
}

fun MediaMetadataRetriever.videoWidth(): Int {
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
}

fun MediaMetadataRetriever.videoSize(): Size {
    return Size(videoWidth(), videoHeight())
}

fun MediaMetadataRetriever.bitrate(): Long {
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE).toLong()
}

fun MediaFormat.mimeType(): String {
    return getString(MediaFormat.KEY_MIME)
}

fun MediaFormat.isVideoFormat(): Boolean {
    return mimeType().startsWith("video/")
}

fun MediaFormat.isAudioFormat(): Boolean {
    return mimeType().startsWith("audio/")
}

fun MediaCodec.inputBufferAt(index: Int): ByteBuffer? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        getInputBuffer(index) else inputBuffers.getOrNull(index)
}

fun MediaCodec.outputBufferAt(index: Int): ByteBuffer? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        getOutputBuffer(index) else outputBuffers.getOrNull(index)
}
