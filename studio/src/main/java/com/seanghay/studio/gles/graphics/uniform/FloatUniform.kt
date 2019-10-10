package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20


fun uniform1f(name: String) = FloatUniform(name)

class FloatUniform(name: String) : Uniform<Float>(name) {

    override fun setValue(value: Float) {
        rationalChecks()
        if (cachedValue == value) return
        GLES20.glUniform1f(getLocation(), value)
        cachedValue = value
    }

    override fun getValue(): Float {
        rationalChecks()
        val args = FloatArray(1)
        GLES20.glGetUniformfv(program, getLocation(), args, 0)
        return args[0]
    }
}