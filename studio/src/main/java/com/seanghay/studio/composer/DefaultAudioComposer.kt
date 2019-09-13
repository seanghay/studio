package com.seanghay.studio.composer

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DefaultAudioComposer(
    private val mediaExtractor: MediaExtractor,
    private val trackIndex: Int,
    private val muxerRender: MuxerRender
): AudioComposer {

    private val sampleType = MuxerRender.SampleType.AUDIO
    private val bufferInfo = MediaCodec.BufferInfo()

    private var isEOS: Boolean = false
    private val actualOutputFormat: MediaFormat = mediaExtractor.getTrackFormat(trackIndex)
    private var bufferSize: Int = actualOutputFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
    private var buffer: ByteBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
    private var writtenPresentationTimeUs: Long = 0

    init { muxerRender.setOutputFormat(sampleType, actualOutputFormat) }

    override fun setup() {
        // ignored
    }

    override fun stepPipeline(): Boolean {
        if (isEOS) return false
        val trackIndex = mediaExtractor.sampleTrackIndex
        if (trackIndex < 0) {
            buffer.clear()
            bufferInfo.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            muxerRender.writeSampleData(sampleType, buffer, bufferInfo)
            isEOS = true
            return true
        }
        if (trackIndex != this.trackIndex) return false

        buffer.clear()
        val sampleSize = mediaExtractor.readSampleData(buffer, 0)
        assert(sampleSize <= bufferSize)
        val isKeyFrame = mediaExtractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0
        val flags = if (isKeyFrame) MediaCodec.BUFFER_FLAG_SYNC_FRAME else 0
        bufferInfo.set(0, sampleSize, mediaExtractor.sampleTime, flags)
        muxerRender.writeSampleData(sampleType, buffer, bufferInfo)
        writtenPresentationTimeUs = bufferInfo.presentationTimeUs

        mediaExtractor.advance()
        return true
    }

    override fun getWrittenPresentationTimeUs(): Long {
        return writtenPresentationTimeUs
    }

    override fun isFinished(): Boolean {
        return isEOS
    }

    override fun release() {
        // ignored
    }

}