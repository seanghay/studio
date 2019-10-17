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

import com.seanghay.studio.gles.graphics.uniform.uniform4f
import com.seanghay.studio.gles.graphics.vec4
import com.seanghay.studio.gles.shader.TextureShader
import com.seanghay.studio.utils.colorArray

class OverlayBlenderFilterShader : TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    var color = vec4(0.0f, 0.0f, 0.0f, 0.0f)

    var colorUniform = uniform4f("overlayColor").autoInit()

    fun setColor(color: Int) {
        this.color.values = color.colorArray
    }

    override fun beforeDrawVertices() {
        super.beforeDrawVertices()
        colorUniform.setValue(color)
    }

    companion object {

        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;

            uniform sampler2D texture;
            uniform vec4 overlayColor;

            varying vec2 texCoord;

            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                gl_FragColor = textureColor + overlayColor;
            }

        """
    }
}
