package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.uniform.FloatUniform
import com.seanghay.studio.gles.shader.TextureShader

class WarmFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var warmth: Float = 0f

    val warmUniform: FloatUniform = FloatUniform("warmth").autoInit()

    override fun beforeDrawVertices() {
        warmUniform.setValue(warmth)
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D texture;
            uniform float warmth;
            
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                
                textureColor.r += warmth;
                textureColor.b -= warmth;
                
                gl_FragColor = textureColor;
            }
        """
    }
}