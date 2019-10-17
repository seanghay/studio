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
