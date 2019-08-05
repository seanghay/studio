package com.seanghay.studio.gles.graphics.texture

import android.graphics.Bitmap
import android.opengl.GLUtils

class BitmapTextureUniform(name: String,
                           private val bitmap: Bitmap,
                           texture2d: Texture2d = Texture2d()
): Texture2dUniform(name, texture2d) {

    override fun configure() {
        super.configure()
        GLUtils.texImage2D(textureTarget, 0, bitmap, 0)
    }
}