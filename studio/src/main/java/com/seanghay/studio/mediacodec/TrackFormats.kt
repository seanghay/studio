package com.seanghay.studio.mediacodec

import android.media.MediaCodecInfo
import android.media.MediaFormat

object TrackFormats {

    @JvmStatic
    fun createVideoFormat(
        width: Int,
        height: Int,
        bitrate: Int
    ): MediaFormat {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        return format
    }


    @JvmStatic
    fun createAudioFormat(
        sampleRate: Int,
        bitrate: Int,
        channelCount: Int
    ): MediaFormat {
        val format =
            MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount)
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 9)
        format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE)
        return format
    }

}