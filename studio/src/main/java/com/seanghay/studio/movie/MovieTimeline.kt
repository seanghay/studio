package com.seanghay.studio.movie

import android.util.Log
import androidx.core.util.rangeTo
import com.seanghay.studio.movie.cache.CacheProvider
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min


data class MovieTimeline(
    val clips: ArrayList<MovieClip> = ArrayList()
): Iterable<MovieClip> {

    var cacheProvider: CacheProvider? = null

    var id: String = generateId()
        private set

    var totalDuration: Long = calculateTotalDuration()
        private set

    fun addClips(vararg clips: MovieClip) {
        this.clips.addAll(clips)
        invalidate()
    }

    fun addClips(clips: List<MovieClip>) {
        this.clips.addAll(clips)
        invalidate()
    }

    fun removeClip(clip: MovieClip) {
        clips.remove(clip)
        invalidate()
    }

    fun removeAt(index: Int) {
        clips.removeAt(index)
        invalidate()
    }

    fun clearClips() {
        clips.clear()
        invalidate()
    }

    fun seekProgress(progress: Float) {
        val percentage = progress.range(0f, 1f)
        seek((percentage * totalDuration).toLong())
    }


    fun seek(at: Long) {

        val seekAt = at rangeIn 0..totalDuration
        var currentDuration = 0L
        var prevDuration = 0L
        var currentSlideDuration = 0L

        val slide = clips.find { clip ->
            currentDuration += clip.getTotalDuration()
            (seekAt in (prevDuration..currentDuration)).also {
                prevDuration += clip.getTotalDuration()
                if (!it) currentSlideDuration = currentDuration
            }
        }

        // val indexOfClip = clips.indexOf(slide)

        val additional = seekAt - currentSlideDuration
        slide?.renderAt(additional)

    }

    override fun iterator(): Iterator<MovieClip> {
        return clips.iterator()
    }

    private fun invalidate() {
        invalidateDuration()
    }

    private fun invalidateDuration() {
        totalDuration = calculateTotalDuration()
    }

    private fun calculateTotalDuration(): Long {
        var duration = 0L
        for (clip in clips) duration += clip.getTotalDuration()
        return duration
    }


    private fun Float.range(start: Float, end: Float): Float {
        return min(max(this, start), end)
    }

    private infix fun Long.rangeIn(range: LongRange): Long {
        return min(max(this, range.first), range.last)
    }

    fun cloneToCache(): MovieTimeline {
        val cacheDir = cacheProvider?.cacheDirectory() ?:
            throw NullPointerException("Cache provider was null")

        val newClips = clips.map { it.cloneToCache(cacheDir, id) }
        return copy(clips = ArrayList(newClips)).also {
            it.invalidate()
        }
    }

    /**
     * Move all paths to cache directory
     */
    fun moveToCache() {
        val cacheDir = cacheProvider?.cacheDirectory() ?:
            throw NullPointerException("Cache provider was null")

        val newClips = clips.map { it.cloneToCache(cacheDir, id) }

        clearClips()
        addClips(newClips)
        invalidate()
    }

    private fun generateId(): String {
        return UUID.randomUUID().toString()
    }
}