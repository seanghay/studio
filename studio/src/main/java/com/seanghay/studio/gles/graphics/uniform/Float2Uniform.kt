package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20.*
import com.seanghay.studio.gles.graphics.Vector2f

fun uniform2f(name: String) = Float2Uniform(name)

class Float2Uniform(name: String): Uniform<Vector2f>(name) {

    override fun setValue(value: Vector2f) {
        rationalChecks()
        glUniform2f(getLocation(), value.x, value.y)
        cachedValue = value
    }

    override fun getValue(): Vector2f {
        rationalChecks()
        val args = FloatArray(2)
        glGetUniformfv(program, getLocation(), args, 0)
        return Vector2f.fromArray(args)
    }
}