package com.seanghay.studio.gles.graphics.texture

import android.opengl.GLES20
import com.seanghay.studio.gles.graphics.texture.Texture

open class Texture2d: Texture() {

    override fun createTexture() {
        super.createTexture()
    }

    open fun configure(textureTarget: Int) {
        GLES20.glTexParameteri(
            textureTarget,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            textureTarget,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            textureTarget,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            textureTarget,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
    }
}