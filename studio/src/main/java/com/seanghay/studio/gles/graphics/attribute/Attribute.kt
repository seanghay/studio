package com.seanghay.studio.gles.graphics.attribute

import android.opengl.GLES20
import com.seanghay.studio.gles.graphics.InputValue

abstract class Attribute<T>(name: String): InputValue<T>(name) {

    override fun loadLocation(): Int {
        return GLES20.glGetAttribLocation(program, name)
    }

    override fun rationalChecks() {
        if (program == -1) throw RuntimeException("Invalid program")
        if (_location == -1) throw RuntimeException("Attribute name: $name is not found!")
    }
}