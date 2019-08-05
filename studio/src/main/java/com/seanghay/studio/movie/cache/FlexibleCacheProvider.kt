package com.seanghay.studio.movie.cache

import android.content.Context
import java.io.File

class FlexibleCacheProvider(private val context: Context): CacheProvider {

    /**
     * Some devices don't have external storage, so we fallback to the internal one.
     */
    override fun cacheDirectory(): File {
        return context.externalCacheDir ?: context.cacheDir
    }

}