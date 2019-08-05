package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20

internal fun uniform1i(name: String) = IntUniform(name)

class IntUniform(name: String): Uniform<Int>(name) {

    override fun setValue(value: Int) {
        rationalChecks()
        GLES20.glUniform1i(getLocation(), value)
    }

    override fun getValue(): Int {
        rationalChecks()
        val args = IntArray(1)
        GLES20.glGetUniformiv(program, getLocation(), args, 0)
        return args[0]
    }
}


