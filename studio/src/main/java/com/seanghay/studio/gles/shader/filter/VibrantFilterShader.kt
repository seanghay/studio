package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.shader.TextureShader

class VibrantFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var vibrant = 0f

    var vibrantUniform = uniform1f("vibrant").autoInit()


    override fun beforeDrawVertices() {
        super.beforeDrawVertices()
        vibrantUniform.setValue(vibrant)
    }

    companion object {

        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D texture;
            uniform float vibrant;
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);

                float mx = max(max(textureColor.r, textureColor.g), textureColor.b);
                float average = (textureColor.r + textureColor.g + textureColor.b) / 3.0;
                float amt = (mx - average) * (-vibrant * 3.0);

                textureColor.rgb = mix(textureColor.rgb, vec3(mx), amt);

                gl_FragColor = textureColor;
            }
            
        """

    }
}