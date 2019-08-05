package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.shader.TextureShader

class GrayscaleFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {
    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            const vec3 W = vec3(0.2125, 0.7154, 0.0721);
            uniform sampler2D texture;
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                float luminance = dot(textureColor.rgb, W);
                gl_FragColor = vec4(vec3(luminance), textureColor.a);
            }
        """
    }
}