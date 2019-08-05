package com.seanghay.studio.gles.graphics



internal fun vec2(x: Float, y: Float) = Vector2f(x, y)
internal fun vec2(x: Float) = Vector2f(x, x)
internal fun vec2() = Vector2f()

data class Vector2f(var x: Float = 0f,
                    var y: Float = 0f) {

    val values get() = floatArrayOf(x, y)

    // TODO: Add transformations Add, Sub, Multiply, Divide

    companion object {
        fun valueOf(vararg values: Float): Vector2f {
            return Vector2f(values[0], values[1])
        }

        fun fromArray(values: FloatArray): Vector2f {
            return Vector2f(values[0], values[1])
        }

    }
}

