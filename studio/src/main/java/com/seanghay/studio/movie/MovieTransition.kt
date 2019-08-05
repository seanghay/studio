package com.seanghay.studio.movie

interface MovieTransition {
    fun getName(): String
    fun getDuration(): Long
}


class FadeTransition: MovieTransition {

    override fun getDuration(): Long {
        return 1000L
    }

    override fun getName(): String {
        return "fade"
    }
}


class ZoomTransition: MovieTransition {

    override fun getDuration(): Long {
        return 1000L
    }

    override fun getName(): String {
        return "zoom"
    }

}