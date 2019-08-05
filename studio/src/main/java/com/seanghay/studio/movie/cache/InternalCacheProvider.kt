package com.seanghay.studio.movie.cache

import android.content.Context
import java.io.File

class InternalCacheProvider(private val context: Context): CacheProvider {
    override fun cacheDirectory(): File {
        return context.cacheDir
    }
}