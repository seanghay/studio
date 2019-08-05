package com.seanghay.studio.movie.cache

import java.io.File

interface CacheProvider {
    fun cacheDirectory(): File
}