package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.shader.TextureShader


class GammaFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var gamma = 1f
    var gammaUniform = uniform1f("gamma").autoInit()

    override fun beforeDrawVertices() {
        super.beforeDrawVertices()
        gammaUniform.setValue(gamma)
    }

    companion object {

        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D texture;
            uniform float gamma;
            
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);
            }
            
        """
    }
}
