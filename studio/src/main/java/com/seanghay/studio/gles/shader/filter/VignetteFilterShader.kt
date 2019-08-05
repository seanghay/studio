package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.shader.TextureShader

class VignetteFilterShader : TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var innerRadius = 0.15f
    var outerRadius = 1.35f
    var opacity = 0.85f

    var innerRadiusUniform = uniform1f("innerRadius").autoInit()
    var outerRadiusUniform = uniform1f("outerRadius").autoInit()
    var opacityUniform = uniform1f("opacity").autoInit()

    override fun beforeDrawVertices() {
        super.beforeDrawVertices()
        innerRadiusUniform.setValue(innerRadius)
        outerRadiusUniform.setValue(outerRadius)
        opacityUniform.setValue(opacity)
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D texture;
            uniform vec2 resolution;

            uniform float innerRadius;
            uniform float outerRadius;
            uniform float opacity;

            varying vec2 texCoord;
             
            void main() {
                
                vec2 centered = (texCoord - vec2(0.5)) * (resolution.x / resolution.y);
                vec4 textureColor = texture2D(texture, texCoord);
                
                vec4 color = vec4(1.0);
                color.rgb *= 1.0 - smoothstep(innerRadius, outerRadius, length(centered));
                
                color *= textureColor;
                
                gl_FragColor = mix(textureColor, color, opacity);
            }
            
        """
    }
}