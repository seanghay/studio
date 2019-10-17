/**
 * Designed and developed by Seanghay Yath (@seanghay)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seanghay.studio.utils

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
