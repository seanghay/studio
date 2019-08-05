package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20

class BooleanUniform(name: String): Uniform<Boolean>(name) {

    override fun setValue(value: Boolean) {
        rationalChecks()
        GLES20.glUniform1i(
            getLocation(),
            if (value) GLES20.GL_TRUE else GLES20.GL_FALSE
        )
    }

    override fun getValue(): Boolean {
        rationalChecks()
        val args = IntArray(1)
        GLES20.glGetUniformiv(program, getLocation(), args, 0)
        return args[0] == GLES20.GL_TRUE
    }
}