package com.seanghay.studioexample

import android.graphics.Bitmap
import android.media.*
import android.opengl.*
import android.opengl.GLES20.*
import android.util.Log
import android.view.Surface
import com.seanghay.studio.gles.egl.EglCore
import com.seanghay.studio.gles.egl.EglWindowSurface
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class VideoCreate(
    private val files: List<File>,
    private val outputDir: File,
    private val bgmPath: String,
    private val progressListener: (Int) -> Unit,
    private val completed: (String) -> Unit) {



    private var renderer: ImageRender? = null

    private var eglCore: EglCore = EglCore()
    private var width: Int = -1
    private var height: Int = -1
    private var bitrate: Int = -1

    private var encoder: MediaCodec? = null
    private var inputSurface: EglWindowSurface? = null
    private var muxer: MediaMuxer? = null
    private var videoTrackIndex = -1
    private var isMuxerStarted = false
    private var bufferInfo = MediaCodec.BufferInfo()

    private var extractor: MediaExtractor? = null

    private var bgmEncoder: MediaCodec? = null
    private var bgmBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
    private var bgmTrackIndex = -1
    private var audioTrackIndex = -1

    private var outputFile: String = ""

    fun configure(width: Int, height: Int, bitrate: Int) {
        this.width = width
        this.height = height
        this.bitrate = bitrate
    }

    fun startEncoding() {

        val startedAt = System.nanoTime()

        try {
            configureEncoder()
            configureBgmEncoder()
            inputSurface?.makeCurrent()

            renderer = ImageRender(files, width, height)

            for (i in 0 until FRAME_COUNT) {
                drainEncoder(false)
                generateSurfaceFrame(i.toInt())
                inputSurface!!.setPresentationTime(computePresentationTimeNsec(i.toInt()))
                Log.d(TAG, "sending frame $i to encoder")
                inputSurface!!.swapBuffers()

                val progress = round((i.toFloat() / FRAME_COUNT.toFloat()) * 100f * 10f) / 10f
                Log.d(TAG, "progress=$progress")
                progressListener(progress.roundToInt())
            }

            drainEncoder(false)
        } finally {
            releaseEncoder()
            completed(outputFile)
        }

        val timeTaken = (System.nanoTime() - startedAt) / ONE_BILLION
        Log.d(TAG, "time_taken=$timeTaken seconds")


    }

    private fun configureBgmEncoder() {
        extractor = MediaExtractor()
        extractor!!.setDataSource(bgmPath)

        for (trackIndex in 0 until extractor!!.trackCount) {
            val format = extractor?.getTrackFormat(trackIndex)!!
            val mimeType = format.getString(MediaFormat.KEY_MIME)
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE)

            Log.d(TAG, "sample_rate=$sampleRate")
            if (mimeType.startsWith("audio")){
                this.bgmTrackIndex = trackIndex
                audioTrackIndex = muxer!!.addTrack(format)
                Log.d(TAG, "Audio format: $format")
            }
        }

        if (bgmTrackIndex == -1) {
            throw RuntimeException("Cannot find audio track!")
        }

        extractor!!.selectTrack(bgmTrackIndex)
        extractor!!.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
    }


    private fun drain(endOfStream: Boolean) {
        val bufferSize = MAX_SAMPLE_SIZE
        val frameCount = 0
        val offset = 100


        val dstBuf = ByteBuffer.allocate(bufferSize)
        val bufInfo = MediaCodec.BufferInfo()

        muxer!!.start()
        isMuxerStarted = true

        while (true) {
            bufInfo.offset = offset
            bufInfo.size = extractor!!.readSampleData(dstBuf, offset)
        }
    }


    private fun drainEncoder(endOfStream: Boolean) {
        val TIMEOUT_SEC = 1000L
        Log.d(TAG, "drainEncoder($endOfStream)")

        if (endOfStream) {
            Log.d(TAG, "sending EndOfStream to encoder")
            encoder!!.signalEndOfInputStream()
        }

        var encoderOutputBuffers  = encoder!!.outputBuffers

        var bufferSize = MAX_SAMPLE_SIZE
        val audioBufferInfo = MediaCodec.BufferInfo()
        val dstBuf = ByteBuffer.allocate(bufferSize)
        var audioDone = false


        while (true) {

            val encoderStatus = encoder!!.dequeueOutputBuffer(bufferInfo, TIMEOUT_SEC)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {

                if (!endOfStream) {
                    break
                } else {
                    Log.d(TAG, "No output buffer available, spinning to await EOS")
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = encoder!!.outputBuffers
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (isMuxerStarted) {
                    throw RuntimeException("Format changed twice")
                }

                val format = encoder!!.outputFormat
                Log.d(TAG, "encoder output format changed: $format")
                videoTrackIndex = muxer!!.addTrack(format)
                muxer!!.start()
                isMuxerStarted = true
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
            } else {

                val encodedData = encoderOutputBuffers[encoderStatus] ?: throw RuntimeException("encoderOutputBuffer[$encoderStatus] was null")

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG")
                    bufferInfo.size = 0
                }

                if (bufferInfo.size != 0) {
                    if (!isMuxerStarted) {
                        throw RuntimeException("muxer hasn't started yet!")
                    }

                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)

                    muxer!!.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    Log.d(TAG, "sent ${bufferInfo.size} bytes to muxer")

                    if (!audioDone) {

                        audioBufferInfo.offset = bufferInfo.offset
                        audioBufferInfo.size = extractor!!.readSampleData(dstBuf, audioBufferInfo.offset)

                        if (audioBufferInfo.size != 0) {
                            audioBufferInfo.presentationTimeUs = extractor!!.sampleTime
                            audioBufferInfo.flags = extractor!!.sampleFlags
                            muxer!!.writeSampleData(audioTrackIndex, dstBuf, audioBufferInfo)
                            extractor!!.advance()
                        } else audioDone = true
                    }
                }

                encoder!!.releaseOutputBuffer(encoderStatus, false)

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly")
                    } else {
                        Log.d(TAG, "EndOfStream reached")
                    }
                    break
                }

            }
        }

    }

    private fun configureEncoder() {

        if (width == -1 || height == -1 || bitrate == -1) {
            throw RuntimeException("Width, height, bitrate was not configured")
        }

        eglCore.setup()

        bufferInfo = MediaCodec.BufferInfo()
        val format = MediaFormat.createVideoFormat(OUTPUT_MIME_TYPE, width, height)

        with(format) {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE.toInt())
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL.toInt())

            Log.d(TAG, "format=$this")
        }

        encoder = MediaCodec.createEncoderByType(OUTPUT_MIME_TYPE).also {

            it.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = EglWindowSurface(eglCore, it.createInputSurface(), false)
            it.start()
        }

        val outputFile = File(outputDir, "test_video ${System.currentTimeMillis()}.mp4").path
        Log.d(TAG, "output_file=$outputFile")
        this.outputFile = outputFile

        try {
            muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (ioe: IOException) {
            throw RuntimeException("MediaMuxer creation failed", ioe)
        }

        videoTrackIndex = -1
        isMuxerStarted = false
    }

    private fun releaseEncoder() {
        Log.d(TAG, "Releasing Encoder object")
        encoder?.stop()
        encoder?.release()
        encoder = null

        inputSurface?.release()
        inputSurface = null

        muxer?.stop()
        muxer?.release()
        muxer = null
    }

    private fun generateSurfaceFrame(i: Int) {
        renderer!!.renderAt(i)
    }

    companion object {
        private val MAX_SAMPLE_SIZE = 256 * 1024
        private const val TAG = "VideoCreate"

        const val OUTPUT_MIME_TYPE = "video/avc"
        const val FRAME_RATE = 30L // 30fps
        const val IFRAME_INTERVAL = 30L
        const val ONE_BILLION = 1_000_000_000L
        const val FRAME_COUNT = FRAME_RATE * 60

        private const val TEST_R0 = 0
        private const val TEST_G0 = 136
        private const val TEST_B0 = 0
        private const val TEST_R1 = 236
        private const val TEST_G1 = 50
        private const val TEST_B1 = 186

        private fun computePresentationTimeNsec(frameIndex: Int): Long {
            return frameIndex * ONE_BILLION / FRAME_RATE
        }
    }

    /*private class CodecInputSurface(surface: Surface?) {

        private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
        private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
        private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

        private var surface: Surface? = surface ?: throw RuntimeException("Surface was null")

        fun setup() {
            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
                throw RuntimeException("unable to get EGL14 Display")
            }

            val version = IntArray(2)
            if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
                throw RuntimeException("unable to initialize EGL14")
            }

            val attribList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
            )

            val configs = arrayOfNulls<EGLConfig>(1)
            val numOfConfigs = IntArray(1)

            EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, numOfConfigs, 0)
            checkEglError("eglCreateContext RGB888+recordable ES2")

            val attrib_list = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )

            eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, attrib_list, 0)
            checkEglError("eglCreateContext")

            val surfaceAttrib = intArrayOf(EGL14.EGL_NONE)

            eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], surface, surfaceAttrib, 0)
            checkEglError("eglCreateWindowSurface")
        }


        fun release() {
            if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglDestroySurface(eglDisplay, eglSurface)
                EGL14.eglDestroyContext(eglDisplay, eglContext)
                EGL14.eglReleaseThread()
                EGL14.eglTerminate(eglDisplay)
            }

            surface?.release()
            eglDisplay = EGL14.EGL_NO_DISPLAY
            eglContext = EGL14.EGL_NO_CONTEXT
            eglSurface = EGL14.EGL_NO_SURFACE
            surface = null
        }

        fun makeCurrent() {
            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
            checkEglError("eglMakeCurrent")
        }


        fun swapBuffers(): Boolean {
            val result = EGL14.eglSwapBuffers(eglDisplay, eglSurface)
            checkEglError("eglSwapBuffers")
            return result
        }

        fun setPresentationTime(nsecs: Long) {
            EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, nsecs)
            checkEglError("elgPresentationTimeANDROID")
        }

        private fun checkEglError(msg: String) {
            val error = EGL14.eglGetError()
            if (error != EGL14.EGL_SUCCESS) {
                throw RuntimeException("$msg : EGL Error: 0x${error.toString(16)}")
            }
        }

        companion object {
            const val EGL_RECORDABLE_ANDROID = 0x3142
        }
    }*/
}