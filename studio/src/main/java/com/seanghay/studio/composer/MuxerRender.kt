package com.seanghay.studio.composer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import com.seanghay.studio.composer.MuxerRender.SampleType.AUDIO
import com.seanghay.studio.composer.MuxerRender.SampleType.VIDEO
import java.nio.ByteBuffer
import java.nio.ByteOrder


class MuxerRender(private val muxer: MediaMuxer) {

    private val sampleInfoList = arrayListOf<SampleInfo>()
    private var isStarted: Boolean = false

    private var audioFormat: MediaFormat? = null
    private var videoFormat: MediaFormat? = null
    private var audioTrackIndex: Int = -1
    private var videoTrackIndex: Int = -1
    private var byteBuffer: ByteBuffer? = null


    fun setOutputFormat(sampleType: SampleType, format: MediaFormat) {
        when (sampleType) {
            VIDEO -> videoFormat = format
            AUDIO -> audioFormat = format
        }
    }

    fun onOutputFormatChanged() {
        if (videoFormat != null && audioFormat != null) {

            videoTrackIndex = muxer.addTrack(videoFormat!!)
            Log.v(TAG, "Added track #" + videoTrackIndex + " with " + videoFormat!!.getString(MediaFormat.KEY_MIME) + " to muxer")
            audioTrackIndex = muxer.addTrack(audioFormat!!)

            Log.v(
                TAG,
                "Added track #" + audioTrackIndex + " with " + audioFormat!!.getString(MediaFormat.KEY_MIME) + " to muxer"
            )

        } else if (videoFormat != null) {

            videoTrackIndex = muxer.addTrack(videoFormat)
            Log.v(
                TAG,
                "Added track #" + videoTrackIndex + " with " + videoFormat!!.getString(MediaFormat.KEY_MIME) + " to muxer"
            )

        }

        muxer.start()
        isStarted = true

        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(0)
        }
        byteBuffer!!.flip()
        Log.v(
            TAG, "Output format determined, writing " + sampleInfoList.size +
                    " samples / " + byteBuffer!!.limit() + " bytes to muxer."
        )
        val bufferInfo = MediaCodec.BufferInfo()
        var offset = 0
        for (sampleInfo in sampleInfoList) {
            sampleInfo.writeToBufferInfo(bufferInfo, offset)
            muxer.writeSampleData(trackIndexOf(sampleInfo.sampleType), byteBuffer!!, bufferInfo)
            offset += sampleInfo.size
        }
        sampleInfoList.clear()
        byteBuffer = null
    }

    fun writeSampleData(
        sampleType: SampleType,
        byteBuf: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        if (isStarted) {
            muxer.writeSampleData(trackIndexOf(sampleType), byteBuf, bufferInfo)
            return
        }

        byteBuf.limit(bufferInfo.offset + bufferInfo.size)
        byteBuf.position(bufferInfo.offset)
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(ByteOrder.nativeOrder())
        }
        byteBuffer!!.put(byteBuf)
        sampleInfoList.add(SampleInfo(sampleType, bufferInfo.size, bufferInfo))
    }

    private fun trackIndexOf(sampleType: SampleType): Int {
        return when (sampleType) {
            VIDEO -> videoTrackIndex
            AUDIO -> audioTrackIndex
        }
    }

    enum class SampleType { VIDEO, AUDIO }

    private data class SampleInfo(
        var sampleType: SampleType,
        var size: Int,
        var bufferInfo: MediaCodec.BufferInfo
    ) {

        val presentationTimeUs: Long get() = bufferInfo.presentationTimeUs
        val flags: Int get() = bufferInfo.flags

        fun writeToBufferInfo(bufferInfo: MediaCodec.BufferInfo, offset: Int) {
            bufferInfo.set(offset, size, presentationTimeUs, flags)
        }
    }


    companion object {
        private const val TAG = "MuxerRender"
        private const val BUFFER_SIZE = 64 * 1024 // Magic number
    }

}