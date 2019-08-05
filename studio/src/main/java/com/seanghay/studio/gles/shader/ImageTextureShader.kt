package com.seanghay.studio.gles.shader

import android.graphics.Bitmap
import com.seanghay.studio.gles.graphics.texture.BitmapTextureUniform
import com.seanghay.studio.gles.graphics.texture.Texture2dUniform


open class ImageTextureShader(var bitmap: Bitmap): TextureShader() {

    var texture2d: Texture2dUniform = BitmapTextureUniform("texture", bitmap).autoInit()

    open fun draw() {
        draw(texture2d.texture)
    }
}