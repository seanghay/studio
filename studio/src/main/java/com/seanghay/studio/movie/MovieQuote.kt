package com.seanghay.studio.movie

import android.graphics.PointF

data class MovieQuote(
    var text: String,
    var position: PointF = PointF(0f, 0f),
    var scale: Float = 1f,
    var fontFamily: String = "Roboto",
    var fontSize: Float = 16f
)