package com.seanghay.studio.engine

import android.media.*
import android.os.Build
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.seanghay.studio.internal.logger
import java.nio.ByteBuffer

@Suppress("Unused")
class AudioTranscoder(
    audioPath: String,
    outputPath: String
) {

    var trimStartUs = 0L
    var trimEndUs = Long.MAX_VALUE

    private val logger = logger()
    private var extractor: MediaExtractor = MediaExtractor()
    private var decoder: MediaCodec
    private var encoder: MediaCodec
    private var outputFormat: MediaFormat
    private var audioTrack: Int = -1
    private var muxer: MediaMuxer
    private var isMuxerStarted = false
    private var outputTrackId = -1

    init {
        // Configure Extractor
        extractor.setDataSource(audioPath)
        audioTrack = extractor.findAudioTrack()
        extractor.selectTrack(audioTrack)

        val audioTrackFormat: MediaFormat = extractor.getTrackFormat(audioTrack)
        val audioMimeType = audioTrackFormat.mimeType()
        val audioDuration = audioTrackFormat.getLong(MediaFormat.KEY_DURATION)

        // Trim audio
        extractor.seekTo(trimStartUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

        val codecInfo = MediaCodecUtil.getDecoderInfo(audioMimeType, false, false)
            ?: throw RuntimeException("CodecInfo not found")

        // Configure Decoder
        decoder = MediaCodec.createByCodecName(codecInfo.name)
        decoder.configure(audioTrackFormat, null, null, 0)
        decoder.start()

        // Configure Encoder
        val outputMimeType = MediaFormat.MIMETYPE_AUDIO_AAC
        outputFormat = createAudioFormat()
        encoder = MediaCodec.createEncoderByType(outputMimeType)
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        // Configure Muxer
        muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        outputTrackId = muxer.addTrack(encoder.outputFormat)
        muxer.start()
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
                    feedEncoder(encoder, buffer, bufferInfo)
                }

                codec.releaseOutputBuffer(outIndex, false)
            } else logger.i("Unexpected Index from output buffer")

            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                logger.d("End of stream")
                feedEncoder(encoder, ByteBuffer.allocate(1), MediaCodec.BufferInfo(), true)
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
                    muxer.writeSampleData(outputTrackId, encodedData, info)
                    logger.d("Sent to muxer: ${info.presentationTimeUs}")
                }
                codec.releaseOutputBuffer(outIndex, false)
            }

            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                logger.d("Encoder reached: End of stream")
                break
            }
        }
    }


    fun setup() {
        logger.d("Setup")
    }

    fun release() {
        logger.d("Release")
        extractor.release()

        muxer.stop()
        muxer.release()

        decoder.stop()
        decoder.release()

        encoder.stop()
        encoder.release()
    }

    @Suppress("Unused")
    fun transcode() {
        logger.d("Starting decoding")
        decode(decoder, extractor)
        logger.d("Finished decoding")
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
