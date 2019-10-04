package com.seanghay.studioexample

import android.media.MediaCodec
import android.media.MediaCodec.*
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import com.seanghay.studio.core.Studio
import com.seanghay.studio.core.StudioDrawable
import com.seanghay.studio.utils.outputBufferAt
import java.io.File


class Mp4Composer(
    private val studio: Studio,
    private val drawable: StudioDrawable,
    private val outputPath: String,
    private val duration: Long,
    val onFinished: () -> Unit
) {

    var width: Int = 1920
    var height: Int = 1080

    var frameRate = 30L

    var iframeInterval = 10
    val frameCount = frameRate * duration / 1000L
    var bitrate = 41_600_000
    var mimeType = "video/avc"

    var bufferInfo = BufferInfo()
    var encoder: MediaCodec? = null
    var muxer: MediaMuxer? = null

    var isMuxerStarted = false
    var trackIndex = -1


    private lateinit var surface: Surface


    private fun computePresentationTimeNsec(frameIndex: Long): Long {
        val ONE_BILLION: Long = 1000000000
        return frameIndex * ONE_BILLION / frameRate
    }


    fun create() {

        bufferInfo = BufferInfo()
        val format = createVideoFormat()
        encoder = createEncoderByType(mimeType).also {
            it.configure(format, null, null, CONFIGURE_FLAG_ENCODE)
            surface = it.createInputSurface()
            it.start()
            muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            isMuxerStarted = false
            trackIndex = -1
        }
    }

    fun start() {

        try {
            // val file = File(outputPath)
            // if (file.exists()) file.delete()

            val exportSurface = studio.createOutputSurface()
            exportSurface.fromSurface(surface)
            studio.setOutputSurface(exportSurface)

            // Make Current
            studio.post {
                for (i in 0 until frameCount) {
                    val progress = i.toFloat() / frameCount.toFloat()
                    drawable.renderAtProgress(progress)
                    studio.directDraw({
                        drainEncoder(false)
                    }, {
                        exportSurface.eglSurface.setPresentationTime(computePresentationTimeNsec(i))
                    })

                    Log.d("Mp4Composer", "Generated frame: $i, Progress: $progress")
                }
                drainEncoder(true)
                release()
                onFinished()
            }

        } finally {
            // release()
        }
    }


    private fun drainEncoder(endOfStream: Boolean) {
        val encoder = encoder ?: throw RuntimeException("Encoder was null")
        val timeOut = 10000L
        if (endOfStream) {
            encoder.signalEndOfInputStream()
        }

        while (true) {
            val encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, timeOut)
            if (encoderStatus == INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
                else {
                    Log.d("Mp4Composer", "no output available, spinning to await EOS")
                }
            } else if (encoderStatus == INFO_OUTPUT_BUFFERS_CHANGED) {

            } else if (encoderStatus == INFO_OUTPUT_FORMAT_CHANGED) {
                if (isMuxerStarted) throw RuntimeException("format changed twice")
                val newFormat = encoder.getOutputFormat()
                trackIndex = muxer!!.addTrack(newFormat)
                muxer!!.start()
                isMuxerStarted = true
            } else if (encoderStatus < 0) {
                Log.w(
                    "Mp4Composer", "unexpected result from encoder.dequeueOutputBuffer: " +
                            encoderStatus
                )
            } else {
                val encodedData = encoder.outputBufferAt(encoderStatus)
                    ?: throw RuntimeException("encoderOutputBuffer $encoderStatus was null")

                if (bufferInfo.flags and BUFFER_FLAG_CODEC_CONFIG != 0) {
                    Log.d("Mp4Composer", "ignoring BUFFER_FLAG_CODEC_CONFIG")
                    bufferInfo.size = 0
                }


                if (bufferInfo.size != 0) {
                    if (!isMuxerStarted) {
                        throw RuntimeException("muxer hasn't started")
                    }

                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)

                    muxer!!.writeSampleData(trackIndex, encodedData, bufferInfo)
                    Log.d("Mp4Composer", "sent " + bufferInfo.size + " bytes to muxer")
                }

                encoder.releaseOutputBuffer(encoderStatus, false)

                if (bufferInfo.flags and BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (!endOfStream) {
                        Log.w("Mp4Composer", "reached end of stream unexpectedly")
                    } else {
                        Log.d("Mp4Composer", "end of stream reached")
                    }

                    break
                }
            }
        }
    }


    fun release() {
        muxer?.apply {
            stop()
            release()
            muxer = null
        }

        encoder?.apply {
            stop()
            release()
            encoder = null
        }


    }

    private fun createVideoFormat(): MediaFormat {
        val format = MediaFormat.createVideoFormat(mimeType, width, height)
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate.toInt())
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval)
        Log.d("Mp4Composer", "format=$format")
        return format
    }
}