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
package com.seanghay.studio.composer

import android.media.MediaCodec
import android.media.MediaFormat
import com.seanghay.studio.utils.inputBufferAt
import com.seanghay.studio.utils.outputBufferAt
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.util.ArrayDeque
import java.util.Queue

class AudioChannel(
  private val encoder: MediaCodec,
  private val decoder: MediaCodec,
  private val encodeFormat: MediaFormat
) {

    private val emptyBuffers: Queue<AudioBuffer> = ArrayDeque()
    private val filledBuffers: Queue<AudioBuffer> = ArrayDeque()
    private val overflowBuffer: AudioBuffer = audioBufferOf()

    private var inputSampleRate: Int = 0
    private var inputChannelCount: Int = 0
    private var outputChannelCount: Int = 0

    private var actualDecodedFormat: MediaFormat? = null

    fun setActualDecodedFormat(format: MediaFormat) {
        inputSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        if (inputSampleRate != encodeFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE))
            throw UnsupportedOperationException("Audio sample rate conversion not supported yet.")

        inputChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        outputChannelCount = encodeFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        if (inputChannelCount != 1 && inputChannelCount != 2)
            throw UnsupportedOperationException("Input channel count ($inputChannelCount) not supported.")

        if (outputChannelCount != 1 && outputChannelCount != 2)
            throw UnsupportedOperationException("OutputSurface channel count ($outputChannelCount) not supported.")

        this.actualDecodedFormat = format
        overflowBuffer.presentationTimeUs = 0
    }

    fun drainDecoderBufferAndQueue(bufferIndex: Int, presentationTimeUs: Long) {
        if (actualDecodedFormat == null)
            throw RuntimeException("Buffer received before format!")

        val data = if (bufferIndex == BUFFER_INDEX_END_OF_STREAM) null
        else decoder.outputBufferAt(bufferIndex)

        val buffer = (emptyBuffers.poll() ?: audioBufferOf()).also {
            it.bufferIndex = bufferIndex
            it.presentationTimeUs = presentationTimeUs
            it.data = data?.asShortBuffer()
        }

        if (overflowBuffer.data == null) {
            overflowBuffer.data = ByteBuffer.allocateDirect(data!!.capacity())
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .also { it.clear().flip() }
        }

        filledBuffers.add(buffer)
    }

    fun feedEncoder(timeoutUs: Long): Boolean {
        val hasOverflow = overflowBuffer.data?.hasRemaining() ?: false
        if (filledBuffers.isEmpty() && !hasOverflow) {
            return false
        }
        val encoderInBuffIndex = encoder.dequeueInputBuffer(timeoutUs)

        if (encoderInBuffIndex < 0) {
            return false
        }

        // Drain overflow first
        val outBuffer = encoder.inputBufferAt(encoderInBuffIndex)?.asShortBuffer()
            ?: throw RuntimeException("InputBuffer was null")
        if (hasOverflow) {
            val presentationTimeUs = drainOverflow(outBuffer)
            encoder.queueInputBuffer(
                encoderInBuffIndex,
                0, outBuffer.position() * BYTES_PER_SHORT,
                presentationTimeUs, 0
            )
            return true
        }

        val inBuffer = filledBuffers.poll()
        if (inBuffer!!.bufferIndex == BUFFER_INDEX_END_OF_STREAM) {
            encoder.queueInputBuffer(
                encoderInBuffIndex,
                0,
                0,
                0,
                MediaCodec.BUFFER_FLAG_END_OF_STREAM
            )
            return false
        }

        val presentationTimeUs = remixAndMaybeFillOverflow(inBuffer, outBuffer)
        encoder.queueInputBuffer(
            encoderInBuffIndex,
            0, outBuffer.position() * BYTES_PER_SHORT,
            presentationTimeUs, 0
        )

        decoder.releaseOutputBuffer(inBuffer.bufferIndex, false)
        emptyBuffers.add(inBuffer)
        return true
    }

    private fun remixAndMaybeFillOverflow(
      input: AudioBuffer,
      outBuff: ShortBuffer
    ): Long {
        val inBuff = input.data!!
        val overflowBuff = overflowBuffer.data!!

        outBuff.clear()

        // Reset position to 0, and set limit to capacity (Since MediaCodec doesn't do that for us)
        inBuff.clear()

        if (inBuff.remaining() > outBuff.remaining()) {
            // Overflow
            // Limit inBuff to outBuff's capacity
            inBuff.limit(outBuff.capacity())
            outBuff.put(inBuff)

            // Reset limit to its own capacity & Keep position
            inBuff.limit(inBuff.capacity())

            // Remix the rest onto overflowBuffer
            // NOTE: We should only reach this point when overflow buffer is empty
            val consumedDurationUs =
                sampleCountToDurationUs(inBuff.position(), inputSampleRate, inputChannelCount)
            overflowBuff.put(inBuff)

            // Seal off overflowBuff & mark limit
            overflowBuff.flip()
            overflowBuffer.presentationTimeUs = input.presentationTimeUs + consumedDurationUs
        } else {
            // No overflow
            outBuff.put(inBuff)
        }

        return input.presentationTimeUs
    }

    private fun drainOverflow(outBuff: ShortBuffer): Long {
        val overflowBuff = overflowBuffer.data
        val overflowLimit = overflowBuff!!.limit()
        val overflowSize = overflowBuff.remaining()

        val beginPresentationTimeUs = overflowBuffer.presentationTimeUs + sampleCountToDurationUs(
            overflowBuff.position(),
            inputSampleRate,
            outputChannelCount
        )

        outBuff.clear()
        // Limit overflowBuff to outBuff's capacity
        overflowBuff.limit(outBuff.capacity())
        // Load overflowBuff onto outBuff
        outBuff.put(overflowBuff)

        if (overflowSize >= outBuff.capacity()) {
            // Overflow fully consumed - Reset
            overflowBuff.clear().limit(0)
        } else {
            // Only partially consumed - Keep position & restore previous limit
            overflowBuff.limit(overflowLimit)
        }

        return beginPresentationTimeUs
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun audioBufferOf(
      bufferIndex: Int = 0,
      presentationTimeUs: Long = 0,
      data: ShortBuffer? = null
    ): AudioBuffer {
        return AudioBuffer(bufferIndex, presentationTimeUs, data)
    }

    private data class AudioBuffer(
      var bufferIndex: Int,
      var presentationTimeUs: Long,
      var data: ShortBuffer?
    )

    companion object {
        const val BUFFER_INDEX_END_OF_STREAM = -1
        const val BYTES_PER_SHORT = 2
        const val MICROSECS_PER_SEC = 1000000L

        private fun sampleCountToDurationUs(
          sampleCount: Int,
          sampleRate: Int,
          channelCount: Int
        ): Long {
            return sampleCount / (sampleRate * MICROSECS_PER_SEC) / channelCount
        }
    }
}
