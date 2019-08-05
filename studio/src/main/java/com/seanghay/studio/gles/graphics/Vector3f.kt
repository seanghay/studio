package com.seanghay.studio.gles.graphics


internal fun vec3(x: Float, y: Float, z: Float) = Vector3f(x, y, z)
internal fun vec3(x: Float) = Vector3f(x, x, x)
internal fun vec3(vec2: Vector2f, z: Float) = Vector3f(vec2.x, vec2.y, z)
internal fun vec3() = Vector3f()


data class Vector3f(var x: Float = 0f,
                    var y: Float = 0f,
                    var z: Float = 0f) {

    val values get() = floatArrayOf(x, y, z)

    // TODO: Add transformations Add, Sub, Multiply, Divide

    companion object {
        fun valueOf(vararg values: Float): Vector3f {
            return Vector3f(values[0], values[1], values[2])
        }

        fun fromArray(values: FloatArray): Vector3f {
            return Vector3f(values[0], values[1], values[2])
        }

    }
}

