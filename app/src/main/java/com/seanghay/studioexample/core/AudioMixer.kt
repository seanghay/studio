package com.seanghay.studioexample.core


import android.media.*
import android.util.SparseArray
import com.seanghay.studio.utils.isAudioFormat
import com.seanghay.studio.utils.isVideoFormat
import java.nio.ByteBuffer
import java.util.*

class AudioMixer(
    private val videoPath: String,
    private val audioPath: String,
    private val outputPath: String
) {

    private val encoder: MediaCodec = MediaCodec.createEncoderByType("video/avc")
    private val muxer: MediaMuxer =
        MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private val trackQueue: Queue<Int> = ArrayDeque()
    private val trackMap: SparseArray<MediaExtractor> = SparseArray()
    private val trackFormats = arrayListOf<MediaFormat>()

    private var isStarted = false
    private var bufferSize: Int = -1

    private var maxUs: Long = -1L


    @Suppress("unused")
    private val videoExtractor: MediaExtractor = MediaExtractor().also {
        it.setDataSource(videoPath)
        val videoFormat = it.selectTrackOf { isVideoFormat() }
        val trackId = muxer.addTrack(videoFormat)
        trackQueue.add(trackId)
        trackMap.put(trackId, it)
        trackFormats.add(videoFormat)
    }

    @Suppress("unused")
    private val audioExtractor: MediaExtractor = MediaExtractor().also {
        it.setDataSource(audioPath)
        val audioFormat = it.selectTrackOf { isAudioFormat() }
        val trackId = muxer.addTrack(audioFormat)
        trackQueue.add(trackId)
        trackMap.put(trackId, it)
        trackFormats.add(audioFormat)
    }

    private val bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()

    fun begin() {

        trackFormats.forEach {
            if (it.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                val maxInputSize = it.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                if (maxInputSize > bufferSize) {
                    bufferSize = maxInputSize
                }
            }

            val duration = it.getLong(MediaFormat.KEY_DURATION)
            if (duration > maxUs) {
                maxUs = duration
            }
        }

        setOrientationHint()

        if (bufferSize < 0) {
            bufferSize = BUFFER_SIZE
        }

        muxer.start()

        val dstBuf = ByteBuffer.allocate(bufferSize)
        val offset = 0
        isStarted = true

        while (!trackQueue.isEmpty()) {

            val trackId = trackQueue.poll()
            val extractor = trackMap.get(trackId)
            extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            var timeOffset = 0L
            var lastTime = 0L

            while (true) {
                bufferInfo.offset = offset
                bufferInfo.size = extractor.readSampleData(dstBuf, offset)
                if (bufferInfo.size < 0) {
                    extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                    timeOffset += lastTime
                    continue
                } else {
                    bufferInfo.presentationTimeUs = extractor.sampleTime + timeOffset
                    lastTime = extractor.sampleTime

                    if (maxUs != -1L && bufferInfo.presentationTimeUs > maxUs) {
                        break
                    }
                    bufferInfo.flags = extractor.sampleFlags
                    muxer.writeSampleData(trackId, dstBuf, bufferInfo)
                    extractor.advance()
                }
            }
        }

        isStarted = false
        muxer.stop()
    }

    private fun setOrientationHint() {
        val retriever = MediaMetadataRetriever()
        retriever.use {
            it.setDataSource(videoPath)
            val orientationString: String? =
                it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            orientationString?.toInt()?.let { if (it >= 0) muxer.setOrientationHint(it) }
        }
    }

    fun flush() {
        encoder.flush()
    }

    private companion object {
        private const val BUFFER_SIZE = 1 * 1024 * 1024 // Magic number
        private fun MediaExtractor.selectTrackOf(block: MediaFormat.() -> Boolean): MediaFormat {
            for (trackIndex in 0 until trackCount) {
                val format = getTrackFormat(trackIndex)
                if (block(format)) {
                    selectTrack(trackIndex)
                    return format
                }
            }

            throw RuntimeException("Couldn't find the preferred MediaFormat track index!")
        }

    }
}