/**
 * Designed and developed by Seanghay Yath (@seanghay)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seanghay.studio.gles.shader.filter

import com.seanghay.studio.gles.graphics.mat4
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniformMat4
import com.seanghay.studio.gles.shader.TextureShader

open class ColorMatrixFilterShader : TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

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
