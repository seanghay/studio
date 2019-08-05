package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20.*
import com.seanghay.studio.gles.graphics.Vector4f


fun uniform4f(name: String) = Float4Uniform(name)

class Float4Uniform(name: String): Uniform<Vector4f>(name) {

    override fun setValue(value: Vector4f) {
        rationalChecks()
        glUniform4f(getLocation(), value.x, value.y, value.z, value.a)
        cachedValue = value
    }

    override fun getValue(): Vector4f {
        rationalChecks()
        val args = FloatArray(4)
        glGetUniformfv(program, getLocation(), args, 0)
        return Vector4f.fromArray(args)
    }
}