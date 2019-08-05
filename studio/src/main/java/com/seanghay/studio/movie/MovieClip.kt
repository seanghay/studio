package com.seanghay.studio.movie

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.provider.MediaStore.Video.Thumbnails.MINI_KIND
import android.util.Log
import android.util.Size
import android.webkit.MimeTypeMap
import com.seanghay.studio.gles.RenderContext
import com.seanghay.studio.utils.compressQuality
import com.seanghay.studio.utils.toMetadataRetriever
import com.seanghay.studio.utils.use
import java.io.File
import java.util.*


data class MovieClip(
    var path: String,
    var quote: MovieQuote? = null
): RenderContext {
    override fun onCreated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDraw(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSizeChanged(size: Size) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var contentType: String = parseContentType()

    private var clipType: ClipType = parseClipType()

    private var transition: MovieTransition = FadeTransition()

    private var duration: Long = calculateDuration()

    var width: Int = -1
        private set

    var height: Int = -1
        private set

    init {
        invalidate()
    }


    fun invalidate() {
        invalidateSize()
    }


    private fun invalidateSize() {
        val size = if (clipType == ClipType.Video) path.getVideoSize()
        else path.getImageSize()

        width = size.first
        height = size.second
    }


    fun extractThumbnail(): Bitmap {
        return if (clipType == ClipType.Video) extractThumbnailFromVideo(path) ?:
            throw RuntimeException("Cannot get thumbnail of $path")
        else compressImage(path, THUMBNAIL_COMPRESSION_LEVEL)
    }

    fun getTotalDuration(): Long {
        return duration + transition.getDuration()
    }

    fun getDuration(): Long {
        duration = calculateDuration()
        return duration
    }

    fun setDuration(duration: Long) {
        this.duration = duration
    }

    fun cloneToCache(cacheDir: File, project: String): MovieClip {
        val dir = File(cacheDir, "projects/$project")

        if (!dir.exists())
            dir.mkdirs()

        val oldFile = File(path)
        val newFilename = "${generateId().replace("-", "")}-${oldFile.name}"
        val newFile = File(dir, newFilename)
        oldFile.copyTo(newFile)

        return copy(path = newFile.absolutePath)
    }

    fun renderAt(at: Long) {
        // TODO:
        Log.d("Clip", "ClipRender at: $at")
    }


    private fun calculateDuration(): Long {
        return if (clipType == ClipType.Video) getDurationOfVideo(path)
        else DEFAULT_IMAGE_DURATION
    }


    private fun parseClipType(): ClipType {
        return when {
            contentType.startsWith("video") -> ClipType.Video
            contentType.startsWith("image") -> ClipType.Image
            else -> throw RuntimeException("Cannot determine clipType of $contentType")
        }
    }


    private fun parseContentType(): String {
        return path.getMimeType() ?: throw RuntimeException("cannot determine mime type of file $path")
    }


    enum class ClipType(val value: String) {
        Video("video"),
        Image("image")
    }

    companion object {
        private const val DEFAULT_IMAGE_DURATION = 3 * 1000L // 3s
        private const val DEFAULT_TRANSITION_DURATION = 1000L // 1s
        private const val THUMBNAIL_COMPRESSION_LEVEL = 50


        private fun String.getImageSize(): Pair<Int, Int> {
            return BitmapFactory.decodeFile(this).use {
                width to height
            }
        }

        private fun String.getVideoSize(): Pair<Int, Int> {
            return toMetadataRetriever {
                val width = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
                val height = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
                width to height
            }
        }

        private fun generateId(): String {
            return UUID.randomUUID().toString()
        }

        private fun extractThumbnailFromVideo(path: String): Bitmap? {
            return ThumbnailUtils.createVideoThumbnail(path, MINI_KIND)
                ?.compressQuality(THUMBNAIL_COMPRESSION_LEVEL)
        }

        private fun compressImage(path: String, quality: Int): Bitmap {
            return BitmapFactory.decodeFile(path).compressQuality(quality)
        }

        private fun String.getMimeType(): String? {
            val extension = MimeTypeMap.getFileExtensionFromUrl(this)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

        private fun getDurationOfVideo(path: String): Long {
            return path.toMetadataRetriever {
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
            }
        }
    }
}