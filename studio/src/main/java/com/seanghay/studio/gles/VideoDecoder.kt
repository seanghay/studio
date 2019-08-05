package com.seanghay.studio.gles

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.Surface
import androidx.annotation.CheckResult
import com.seanghay.studio.utils.durationUs
import com.seanghay.studio.utils.inputBufferAt
import com.seanghay.studio.utils.isVideoFormat
import com.seanghay.studio.utils.mimeType
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class VideoDecoder(
    private var surface: Surface,
    private var path: String,
    private var progressListener: ProgressListener? = null
): Thread() {

    private val extractor = MediaExtractor().also {
        it.setDataSource(path)
    }

    private val retriever: MediaMetadataRetriever = MediaMetadataRetriever().also {
        it.setDataSource(path)
    }

    private val videoTrackIndex = firstVideoTrack() ?: throw RuntimeException("Cannot get video track")
    private val videoTrackFormat = extractor.getTrackFormat(videoTrackIndex)
    private val videoMimeType: String = videoTrackFormat.mimeType()
    private val decoder: MediaCodec = MediaCodec.createDecoderByType(videoMimeType)
    private var isEndOfStream = false
    private var videoBufferInfo = MediaCodec.BufferInfo()

    var frameCallback: FrameCallback = SpeedCallback().also {
        it.setFixedPlaybackRate(30)
    }

    private var lock = Object()
    private var isLoop = false
    private var isStarted = false
    private var isPaused = false
    private var isCompleted = false

    private var writtenDuration: Long = 0L
    private var totalDuration: Long = retriever.durationUs()

    override fun start() {
        super.start()
        prepareDecoder()
    }

    override fun run() {
        startDecoding()
    }

    fun setLooping(loop: Boolean) {
        isLoop = loop
    }

    fun pause() {
        synchronized(lock) {
            isPaused = true
            lock.notify()
        }
    }

    fun unpause() {
        synchronized(lock) {
            isPaused = false
            lock.notify()
        }
    }

    // TODO: It's not working correctly now
    fun seek(to: Long) {
        if (to >= 0) {
            extractor.seekTo(to, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            extractor.advance()
        }
    }

    private fun prepareDecoder() {
        selectVideoTrack()
        decoder.configure(videoTrackFormat, surface, null, 0)
        decoder.start()

        synchronized(lock) {
            isStarted = true
            lock.notify()
        }
    }

    private fun selectVideoTrack() {
        extractor.selectTrack(videoTrackIndex)
        seekStart()
    }

    private fun seekStart() {
        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
    }

    private fun waitStart() {
        while(!isStarted) {
            synchronized(lock) {
                try {
                    lock.wait()
                }  catch (ex: InterruptedException) {}
            }
        }
    }

    private fun drainInputBuffers() {
        val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT)
        if (inputBufferIndex >= 0) {
            val inputBuffer = decoder.inputBufferAt(inputBufferIndex)
                ?: throw RuntimeException("InputBuffer was null")

            val chunkSize = extractor.readSampleData(inputBuffer, 0)
            queueInputBuffer(inputBufferIndex, chunkSize)
        }
    }

    private fun queueInputBuffer(index: Int, chunkSize: Int) {
        if (chunkSize < 0) {
            decoder.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            isEndOfStream = true
        } else {
            decoder.queueInputBuffer(index, 0, chunkSize, extractor.sampleTime, 0)
            extractor.advance()
        }
    }


    @CheckResult
    private fun checkOutputBuffers(): Boolean {
        var willLoop = false
        val status = decoder.dequeueOutputBuffer(videoBufferInfo, TIMEOUT)

        if (status >= 0) {
            if (videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                if (isLoop) willLoop = true
            }

            val willRender = videoBufferInfo.size != 0
            if (willRender) frameCallback.preRender(videoBufferInfo.presentationTimeUs)
            decoder.releaseOutputBuffer(status, willRender)

            if (willRender) frameCallback.postRender()

            processChanges()

            if (willLoop) {
                isEndOfStream = false
                seekStart()
                decoder.flush()
                frameCallback.loopReset()
            }

            if (isEndOfStream && !isLoop) return false

        }

        return true
    }


    private fun processChanges() {
        writtenDuration = videoBufferInfo.presentationTimeUs
        val progress = min(max(writtenDuration.toFloat() / totalDuration.toFloat(), 0f), 1f)
        progressListener?.onProgressChanged(progress)

        verbose {
            // Log.d(TAG, "Progress: $progress")
        }
    }


    private fun startDecoding() {
        while (!isInterrupted && !isCompleted) {
            waitStart()
            waitResume()

            if (!isEndOfStream) drainInputBuffers()
            if (!checkOutputBuffers()) break
        }

        decoder.flush()

        synchronized(lock) {
            isCompleted = true
            lock.notify()
        }
    }

    private fun waitResume() {
        synchronized(lock) {
            while(isPaused) {
                try {
                    lock.wait()
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }
            }
        }
    }


    fun release() {
        synchronized(lock) {
            while (!isCompleted) {
                isCompleted = true
                lock.wait()

            }
        }

        decoder.stop()
        extractor.release()
        decoder.release()
        surface.release()
    }


    private fun firstVideoTrack(): Int? {
        for(trackIndex in 0 until extractor.trackCount) {
            val trackFormat = extractor.getTrackFormat(trackIndex)
            if (trackFormat.isVideoFormat()) return trackIndex
        }
        return null
    }


    fun seekProgress(seekProgress: Float) {
        val seekDuration = (seekProgress * totalDuration).toLong()
        seek(seekDuration)
    }

//
//    private fun getStopPosition(start: Long, end: Long): Int {
//        val delta = end - start
//    }


    interface ProgressListener {
        fun onProgressChanged(progress: Float)
    }

    interface DecodingListener {
        fun onStartDecoding()
        fun onDecoding()
        fun onFinishedDecoding()
        fun onRestartDecoding()
    }


    interface FrameCallback {
        fun preRender(presentationTimeUs: Long)
        fun postRender()
        fun loopReset()
    }

    class SpeedCallback: FrameCallback {

        private val oneMillion = 1_000_000L
        private var prevPresentMicroSeconds = 0L
        private var prevMonoMicroSeconds = 0L
        private var fixedFrameDurationMicroSeconds = 0L
        private var loopReset = false

        fun setFixedPlaybackRate(fps: Int) {
            fixedFrameDurationMicroSeconds = oneMillion / fps
        }

        override fun preRender(presentationTimeUs: Long) {
            if (prevMonoMicroSeconds == 0L) {
                prevMonoMicroSeconds = System.nanoTime() / 1000
                prevPresentMicroSeconds = presentationTimeUs
            } else {

                if (loopReset) {
                    prevPresentMicroSeconds = presentationTimeUs - (oneMillion / 30L)
                    loopReset = false
                }

                var frameDelta = if (fixedFrameDurationMicroSeconds != 0L)
                    fixedFrameDurationMicroSeconds else
                    presentationTimeUs - prevPresentMicroSeconds

                when {
                    frameDelta < 0L -> frameDelta = 0
                    frameDelta == 0L -> { }
                    frameDelta > 10 * oneMillion -> frameDelta = 5 * oneMillion
                }

                val desiredMicroSeconds = prevMonoMicroSeconds + frameDelta
                var nowMicroSeconds = System.nanoTime() / 1000
                while (nowMicroSeconds < (desiredMicroSeconds - 100)) {
                    var sleepTimeMicroSeconds = desiredMicroSeconds - nowMicroSeconds
                    if (sleepTimeMicroSeconds > 500000L)
                        sleepTimeMicroSeconds = 500000L
                    try {
                        sleep(sleepTimeMicroSeconds / 1000, (sleepTimeMicroSeconds % 1000).toInt() * 1000)
                    } catch (e: InterruptedException) {}
                    nowMicroSeconds = System.nanoTime() / 1000L
                }

                prevMonoMicroSeconds += frameDelta
                prevPresentMicroSeconds += frameDelta
            }
        }

        override fun postRender() {

        }

        override fun loopReset() {
            loopReset = true
        }

    }

    companion object {

        private const val VERBOSE = true
        private const val TAG = "VideoDecoder"

        private const val TIMEOUT = 1000L

        private inline fun verbose(block: () -> Unit) {
            if (VERBOSE) block()
        }
    }
}