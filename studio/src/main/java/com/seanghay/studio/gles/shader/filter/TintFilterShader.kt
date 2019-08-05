package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.shader.TextureShader

class TintFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var tint = 0f
    var tintUniform = uniform1f("tint").autoInit()

    override fun beforeDrawVertices() {
        super.beforeDrawVertices()
        tintUniform.setValue(tint)
    }


    companion object {

        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D texture;
            uniform float tint;
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                textureColor.g += tint;
                gl_FragColor = textureColor;
            }
            
        """
    }
}