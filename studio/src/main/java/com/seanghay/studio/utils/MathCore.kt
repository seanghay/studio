package com.seanghay.studio.utils

import androidx.annotation.FloatRange
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

/**
 * Constraint output value from input value.
 * @param a Minimum output value
 * @param b Maximum output value
 */
fun Float.clamp(a: Float, b: Float): Float {
    return (min(b, max(a, this)))
}


fun Float.smoothStep(edge0: Float, edge1: Float): Float {
    val x = ((this - edge0) / (edge1 - edge0)).clamp(0f, 1f)
    return (x * x * (3f - 2f * x))
}

/**
 * Linear Interpolate between two values
 * @param start Start value
 * @param end End value
 * @receiver a float range value between 0f..1f
 */
fun Float.lerp(start: Float, end: Float): Float {
    return (1 - this) * start + this * end
}

fun Float.toRadians(): Float {
    return this * PI.toFloat() / 180f
}