package com.seanghay.studio.movie.cache

import android.content.Context
import java.io.File

class ExternalCacheProvider(private val context: Context): CacheProvider {

    override fun cacheDirectory(): File {
        return context.externalCacheDir ?: throw RuntimeException("External cache was null")
    }

}