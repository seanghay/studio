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
package com.seanghay.studioexample.core

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaCodecInfo
import com.seanghay.studio.utils.inputBufferAt
import com.seanghay.studio.utils.isAudioFormat
import com.seanghay.studio.utils.mimeType
import com.seanghay.studio.utils.outputBufferAt

@Deprecated("Deprecated")
class AudioTranscoder(
  private val filePath: String,
  private val outputPath: String
) {
    private val extractor: MediaExtractor = MediaExtractor()
    private val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

    private lateinit var format: MediaFormat
    private lateinit var decoder: MediaCodec

    private var isStarted = false

    private val encoder: MediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm")

    init {
        extractor.setDataSource(filePath)
        for (trackId in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(trackId)
            val mimeType = format.mimeType()
            if (format.isAudioFormat()) {
                this.format = format
                decoder = MediaCodec.createDecoderByType(mimeType).also {
                    it.configure(format, null, null, 0)
                }
                extractor.selectTrack(trackId)
                break
            }
        }

        if (!::format.isInitialized) {
            throw RuntimeException("Audio format is not found!")
        }

        if (!::decoder.isInitialized) {
            throw RuntimeException("unable to create decoder because Audio format cannot be found!")
        }

        configureEncoder()
    }

    private fun configureEncoder() {
        val outputFormat = MediaFormat()
        outputFormat.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm")
        outputFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        outputFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100)
        outputFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    fun begin() {
        val timeout = 1000L
        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        var isEndOfStream = false
        val bufferInfo = MediaCodec.BufferInfo()
        decoder.start()
        encoder.start()

        while (true) {
            if (!isEndOfStream) {
                val inputBufferIndex = decoder.dequeueInputBuffer(1000L)
                if (inputBufferIndex >= 0) {
                    val size =
                        extractor.readSampleData(decoder.inputBufferAt(inputBufferIndex)!!, 0)
                    if (size < 0) {
                        // EOS
                        decoder.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isEndOfStream = true
                    } else {
                        decoder.queueInputBuffer(inputBufferIndex, 0, size, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }

            val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, timeout)

            if (outputBufferIndex >= 0) {
                val outputBuffer = decoder.outputBufferAt(outputBufferIndex)!!
                outputBuffer.position(0)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    muxer.writeSampleData(t, outputBuffer, bufferInfo)
                    break
                }

                muxer.writeSampleData(t, outputBuffer, bufferInfo)
                decoder.releaseOutputBuffer(outputBufferIndex, false)
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val format = decoder.outputFormat
                t = muxer.addTrack(format)
                muxer.start()
            }
        }

        isStarted = false
    }

    var t = -1

    fun release() {
        muxer.stop()
        muxer.release()

        encoder.flush()
        encoder.release()
        decoder.flush()
        decoder.release()
        extractor.release()
    }

    companion object {
        private const val TAG = "AudioTranscoder"
    }
}
