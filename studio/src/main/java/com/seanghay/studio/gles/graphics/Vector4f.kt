package com.seanghay.studio.gles.graphics


internal fun vec4(x: Float, y: Float, z: Float, a: Float) = Vector4f(x, y, z, a)
internal fun vec4(x: Float) = Vector4f(x, x, x, x)
internal fun vec4(vec3: Vector2f, z: Float, a: Float) = Vector4f(vec3.x, vec3.y, z, a)
internal fun vec4(vec3: Vector3f, a: Float) = Vector4f(vec3.x, vec3.y, vec3.z, a)
internal fun vec4() = Vector4f()

data class Vector4f(var x: Float = 0f,
                    var y: Float = 0f,
                    var z: Float = 0f,
                    var a: Float = 0f) {

    var values get() = floatArrayOf(x, y, z, a)
        set(value) {
            x = value[0]
            y = value[1]
            z = value[2]
            a = value[3]
        }

    // TODO: Add transformations Add, Sub, Multiply, Divide

    companion object {
        fun valueOf(vararg values: Float): Vector4f {
            return Vector4f(values[0], values[1], values[2], values[3])
        }

        fun fromArray(values: FloatArray): Vector4f {
            return Vector4f(values[0], values[1], values[2], values[3])
        }

    }
}

