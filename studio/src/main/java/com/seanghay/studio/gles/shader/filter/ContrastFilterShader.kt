package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.uniform.FloatUniform
import com.seanghay.studio.gles.shader.TextureShader

class ContrastFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var contrast: Float = 1.0f

    val contrastUniform: FloatUniform = FloatUniform("contrast").autoInit()

    override fun beforeDrawVertices() {
        contrastUniform.setValue(contrast)
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D texture;
            uniform float contrast;
            
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);
            }
            
        """
    }
}