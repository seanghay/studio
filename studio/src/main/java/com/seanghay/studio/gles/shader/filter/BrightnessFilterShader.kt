package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.uniform.FloatUniform
import com.seanghay.studio.gles.shader.TextureShader

class BrightnessFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var brightness: Float = 0f

    val brightnessUniform: FloatUniform = FloatUniform("brightness").autoInit()

    override fun beforeDrawVertices() {
        brightnessUniform.setValue(brightness)
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D texture;
            uniform float brightness;
            
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                
                textureColor.rgb += vec3(brightness);

                gl_FragColor = textureColor;
            }
            
        """
    }
}