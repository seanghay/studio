package com.seanghay.studio.utils

import android.graphics.Color
import androidx.annotation.ColorInt

val Int.colorArray: FloatArray get() {
    return floatArrayOf(
        Color.red(this) / 255f,
        Color.green(this) / 255f,
        Color.blue(this) / 255f,
        Color.alpha(this) / 255f)
}
