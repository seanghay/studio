package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.uniform.FloatUniform
import com.seanghay.studio.gles.shader.TextureShader

class SaturationFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var saturation: Float = 1.0f

    val saturationUniform: FloatUniform = FloatUniform("saturation").autoInit()

    override fun beforeDrawVertices() {
        saturationUniform.setValue(saturation)
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

            uniform sampler2D texture;
            uniform float saturation;
            
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                float luminance = dot(textureColor.rgb, luminanceWeighting);
                vec3 greyScaleColor = vec3(luminance);
                gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturation), textureColor.w);
            }
            
        """
    }
}