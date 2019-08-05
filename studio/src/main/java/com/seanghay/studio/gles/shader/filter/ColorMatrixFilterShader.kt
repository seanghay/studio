package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.mat4
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniformMat4
import com.seanghay.studio.gles.shader.TextureShader

open class ColorMatrixFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var colorMatrix = mat4()

    var intensity = 1.0f

    var colorMatrixUniform = uniformMat4("colorMatrix").autoInit()
    var intensityUniform = uniform1f("intensity").autoInit()

    override fun beforeDrawVertices() {
        super.beforeDrawVertices()
        colorMatrixUniform.setValue(colorMatrix)
        intensityUniform.setValue(intensity)
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            
            precision mediump float;
            
            uniform sampler2D texture;
            uniform mat4 colorMatrix;
            uniform float intensity;
            
            varying vec2 texCoord;
            
            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                vec4 outputColor = textureColor * colorMatrix;
                
                gl_FragColor = (intensity * outputColor) + ((1.0 - intensity) * textureColor);
            }
            
        """
    }
}