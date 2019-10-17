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

import com.seanghay.studio.gles.graphics.uniform.uniform2f
import com.seanghay.studio.gles.graphics.vec2
import com.seanghay.studio.gles.shader.TextureShader

open class GaussianBlurFilterShader : TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var direction = vec2(0.0f, 0.0f)

    var directionUniform = uniform2f("direction").autoInit()

    override fun beforeDrawVertices() {
        super.beforeDrawVertices()
        directionUniform.setValue(direction)
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;

            uniform sampler2D texture;
            uniform vec2 direction;
            uniform vec2 resolution;

            varying vec2 texCoord;

            vec4 blur(sampler2D image, vec2 uv) {
              vec4 color = vec4(0.0);
              vec2 off1 = vec2(1.3846153846) * direction;
              vec2 off2 = vec2(3.2307692308) * direction;
              color += texture2D(image, uv) * 0.2270270270;
              color += texture2D(image, uv + (off1 / resolution)) * 0.3162162162;
              color += texture2D(image, uv - (off1 / resolution)) * 0.3162162162;
              color += texture2D(image, uv + (off2 / resolution)) * 0.0702702703;
              color += texture2D(image, uv - (off2 / resolution)) * 0.0702702703;
              return color;
            }

            void main() {
                // vec4 textureColor = texture2D(texture, texCoord);
                gl_FragColor = blur(texture, texCoord);
            }

        """
    }
}
