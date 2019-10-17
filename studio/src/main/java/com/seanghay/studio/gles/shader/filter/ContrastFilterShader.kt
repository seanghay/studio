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

class ContrastFilterShader : TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var contrast: Float = 1.0f

    val contrastUniform: FloatUniform = FloatUniform("contrast").autoInit()

    override fun beforeDrawVertices() {
        contrastUniform.setValue(contrast)
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;

            uniform sampler2D texture;
            uniform float contrast;

            varying vec2 texCoord;

            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);
            }

        """
    }
}
