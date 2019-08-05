package com.seanghay.studio.core

import android.content.Context
import com.seanghay.studio.gles.RenderTarget
import com.seanghay.studio.movie.MovieComposer
import com.seanghay.studio.movie.MovieTimeline

class PreviewComposer(
    context: Context,
    timeline: MovieTimeline
): MovieComposer(context, timeline) {

    override lateinit var renderTarget: RenderTarget

    override fun onCreate() {
        super.onCreate()
        renderTarget = MovieTexturePreview(context)
    }

}