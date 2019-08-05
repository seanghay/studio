package com.seanghay.studio.gles.graphics.texture

import android.opengl.GLES20

open class Texture2dUniform(name: String, var texture2d: Texture2d): TextureUniform(name, texture2d) {

    override var textureTarget: Int = GLES20.GL_TEXTURE_2D

    override fun configure() {
        texture2d.configure(textureTarget)
    }


    override fun setValue(value: Int) {
        rationalChecks()
        GLES20.glUniform1i(_location, value)
    }

    override fun getValue(): Int {
        val args = IntArray(1)
        GLES20.glGetUniformiv(program, _location, args, 0)
        return args[0]
    }
}