package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20.*
import com.seanghay.studio.gles.graphics.Matrix4f


fun uniformMat4(name: String) = Mat4Uniform(name)

class Mat4Uniform(name: String): Uniform<Matrix4f>(name) {

    override fun setValue(value: Matrix4f) {
        rationalChecks()
        glUniformMatrix4fv(getLocation(), 1, false, value.elements, 0)
        cachedValue = value
    }

    override fun getValue(): Matrix4f {
        rationalChecks()
        val args = FloatArray(4 * 4)
        glGetUniformfv(program, getLocation(), args, 0)
        return Matrix4f(args)
    }

}