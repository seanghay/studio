package com.seanghay.studioexample

import android.media.*
import android.media.MediaCodec.*
import android.os.Build
import android.util.Log
import android.view.Surface
import com.seanghay.studio.core.Studio
import com.seanghay.studio.core.StudioDrawable
import com.seanghay.studio.internal.logger
import com.seanghay.studio.utils.outputBufferAt
import java.nio.ByteBuffer


class Mp4Composer(
    private val studio: Studio,
    private val drawable: StudioDrawable,
    private val outputPath: String,
    private val duration: Long,
    val onFinished: () -> Unit
) {


    private val logger = logger()

    var trimStartUs = 0L
    var trimEndUs = Long.MAX_VALUE

    var audioPath: String? = null

    var onProgressChange: (Float) -> Unit = {}
    var onAudioProgress: (Float) -> Unit = {
        logger.d("AudioProgress: $it")
    }

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

    // Audio
    private var audioEncoder: MediaCodec? = null
    private var audioDecoder: MediaCodec? = null
    private var audioExtractor: MediaExtractor = MediaExtractor()
    private var audioFormat: MediaFormat? = null
    private var audioTrackIndex: Int = -1


    private fun setupAudio() {
        if (!hasAudio()) return

        trimStartUs = 0L
        trimEndUs = duration * 1000L

        audioExtractor = MediaExtractor()
        audioExtractor.setDataSource(audioPath!!)
        val audioTrackIndex = audioExtractor.findAudioTrack()
        audioExtractor.selectTrack(audioTrackIndex)
        val audioFormat = audioExtractor.getTrackFormat(audioTrackIndex)
        val mimeType = audioFormat.mimeType()

        audioDecoder = createDecoderByType(mimeType)
        audioDecoder?.configure(audioFormat, null, null, 0)
        audioDecoder?.start()

        // Configure Encoder
        val outputMimeType = MediaFormat.MIMETYPE_AUDIO_AAC
        this.audioFormat = createAudioFormat()
        audioEncoder = MediaCodec.createEncoderByType(outputMimeType)
        audioEncoder?.configure(this.audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        audioEncoder?.start()
    }

    // Create AAC Audio format
    private fun createAudioFormat(): MediaFormat {
        val format = MediaFormat()
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 9)
        format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44_100)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 320_000)
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE)
        return format
    }


    private fun computePresentationTimeNsec(frameIndex: Long): Long {
        val ONE_BILLION: Long = 1000000000
        return frameIndex * ONE_BILLION / frameRate
    }

    private fun hasAudio(): Boolean {
        return audioPath?.isNotEmpty() ?: false
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

        setupAudio()
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
                    onProgressChange(progress)
                    Log.d("Mp4Composer", "Generated frame: $i, Progress: $progress")
                }
                drainEncoder(true)

                // decode(audioDecoder!!, audioExtractor)

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
                if (hasAudio()) {
                    // audioTrackIndex = muxer?.addTrack(audioFormat!!)!!
                }
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


    /**
     * Decode an audio
     * @param codec Decoder object
     * @param extractor MediaExtractor
     */
    private fun decode(codec: MediaCodec, extractor: MediaExtractor) {
        val timeOut = 5000L
        val bufferInfo = MediaCodec.BufferInfo()

        while (true) {

            // InputBuffers
            val inIndex = codec.dequeueInputBuffer(timeOut)
            if (inIndex >= 0) {
                val buffer = codec.getInputBuffer(inIndex) ?: continue
                val sampleSize = extractor.readSampleData(buffer, 0)

                if (sampleSize < 0) {
                    logger.d("extractor reached end of stream")
                    codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                } else {

                    if (extractor.sampleTime >= trimEndUs) {
                        logger.d("[trim] extractor reached end of stream")
                        codec.queueInputBuffer(
                            inIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        continue
                    }

                    val presentationTimeUs = extractor.sampleTime
                    logger.d("queue inputBuffers")
                    onAudioProgress(presentationTimeUs / trimEndUs.toFloat())
                    codec.queueInputBuffer(inIndex, 0, sampleSize, presentationTimeUs, 0)
                    extractor.advance()
                }
            }

            // OutputBuffers
            val outIndex = codec.dequeueOutputBuffer(bufferInfo, timeOut)

            if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                logger.i("MediaCodec: INFO_TRY_AGAIN_LATER")
            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                logger.i("MediaCodec: INFO_OUTPUT_FORMAT_CHANGED")
            } else if (outIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                logger.d("MediaCodec: INFO_OUTPUT_BUFFERS_CHANGED (Deprecated)")
            } else if (outIndex >= 0) {
                val buffer = codec.getOutputBuffer(outIndex) ?: continue
                logger.d("Decoded data: $buffer")

                if (bufferInfo.size != 0) {
                    buffer.position(bufferInfo.offset)
                    buffer.limit(bufferInfo.size)
                    feedEncoder(audioEncoder!!, buffer, bufferInfo)
                }

                codec.releaseOutputBuffer(outIndex, false)
            } else logger.i("Unexpected Index from output buffer")

            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                logger.d("End of stream")
                feedEncoder(audioEncoder!!, ByteBuffer.allocate(1), MediaCodec.BufferInfo(), true)
                break
            }
        }
    }

    private fun feedEncoder(
        codec: MediaCodec,
        buffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean = false
    ) {

        val info = MediaCodec.BufferInfo()
        val timeout = 10000L
        var isInputDone = false

        while (true) {

            if (!isInputDone) {
                val inIndex = codec.dequeueInputBuffer(timeout)
                if (inIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inIndex) ?: continue
                    inputBuffer.clear()

                    if (endOfStream) {
                        codec.queueInputBuffer(
                            inIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isInputDone = true
                        continue
                    }

                    inputBuffer.put(buffer)

                    codec.queueInputBuffer(
                        inIndex,
                        0,
                        bufferInfo.size,
                        bufferInfo.presentationTimeUs,
                        0
                    )

                    logger.d("Present: ${bufferInfo.presentationTimeUs}")

                    isInputDone = true
                }
            }

            val outIndex = codec.dequeueOutputBuffer(info, timeout)
            if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                logger.i("MediaCodec: INFO_TRY_AGAIN_LATER")
                break
            } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                logger.i("MediaCodec: INFO_OUTPUT_FORMAT_CHANGED")
            } else if (outIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                logger.d("MediaCodec: INFO_OUTPUT_BUFFERS_CHANGED (Deprecated)")
            } else if (outIndex >= 0) {
                val encodedData = codec.getOutputBuffer(outIndex) ?: continue
                logger.d("Encoded data: $encodedData")
                if (info.size != 0) {
                    encodedData.position(info.offset)
                    encodedData.limit(info.offset + info.size)
                    muxer!!.writeSampleData(audioTrackIndex, encodedData, info)
                    logger.d("Sent to muxer: ${info.presentationTimeUs}")
                }
                codec.releaseOutputBuffer(outIndex, false)
            }

            if (bufferInfo.flags and BUFFER_FLAG_END_OF_STREAM != 0) {
                logger.d("Encoder reached: End of stream")
                break
            }
        }
    }


    companion object {
        // Have no idea what is it for
        private const val DEFAULT_BUFFER_SIZE = 4096

        // Read data from media extractor
        // Kinda useless maybe!
        private fun MediaExtractor.readData(buffer: ByteBuffer, size: Int): Int {
            var totalSize = 0
            do {
                val sampleSize: Int =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                        readSampleData(buffer, totalSize)
                    else readSampleData(buffer, 0)

                if (sampleSize < 0) return totalSize
                totalSize += sampleSize
                advance()
            } while (totalSize < size)
            return totalSize
        }


        // Grab mimeType from MediaFormat
        private fun MediaFormat.mimeType(): String {
            return getString(MediaFormat.KEY_MIME)
        }

        // Get the first audio track index of MediaExtractor
        private fun MediaExtractor.findAudioTrack(): Int {
            for (trackIndex in 0 until trackCount) {
                val format = getTrackFormat(trackIndex)
                val mimeType = format.getString(MediaFormat.KEY_MIME)
                if (mimeType.startsWith("audio/"))
                    return trackIndex
            }
            throw RuntimeException("Audio track cannot be found")
        }
    }
}