package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20
import com.seanghay.studio.gles.graphics.InputValue

abstract class Uniform<T>(name: String): InputValue<T>(name) {

    // Sometimes, uniforms don't need to be existed.
    var isOptional = false

    override fun loadLocation(): Int {
        return GLES20.glGetUniformLocation(program, name)
    }

    override fun rationalChecks() {
        if (program == -1) throw RuntimeException("Invalid program")
        if (_location == -1 && !isOptional)
            throw RuntimeException("Uniform name: $name is not found! Did you Initialize it yet?")
    }
}