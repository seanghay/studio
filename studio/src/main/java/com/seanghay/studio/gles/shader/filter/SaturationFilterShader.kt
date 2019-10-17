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

import com.seanghay.studio.gles.graphics.uniform.FloatUniform
import com.seanghay.studio.gles.shader.TextureShader

class SaturationFilterShader : TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

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
