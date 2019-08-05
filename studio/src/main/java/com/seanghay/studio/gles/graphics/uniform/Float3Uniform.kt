package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20.*
import com.seanghay.studio.gles.graphics.Vector3f


fun uniform3f(name: String) = Float3Uniform(name)

class Float3Uniform(name: String): Uniform<Vector3f>(name) {

    override fun setValue(value: Vector3f) {
        rationalChecks()
        glUniform3f(getLocation(), value.x, value.y, value.z)
        cachedValue = value
    }

    override fun getValue(): Vector3f {
        rationalChecks()
        val args = FloatArray(3)
        glGetUniformfv(program, getLocation(), args, 0)
        return Vector3f.fromArray(args)
    }
}