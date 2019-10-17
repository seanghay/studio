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
package com.seanghay.studio.gles.shader

import android.opengl.Matrix
import com.seanghay.studio.gles.graphics.Matrix4f
import com.seanghay.studio.gles.graphics.uniform.uniform1f

open class AlphaOverlayTextureShader :
    TextureShader(fragmentShaderSource = OVERLAY_FRAGMENT_SHADER) {

    open var alpha: Float = 1f
    open var scaleX = 1f
    open var scaleY = 1f
    open var translateX = 0f
    open var translateY = 0f
    open var rotation = 0f

    open var alphaUniform = uniform1f(ALPHA_UNIFORM).autoInit()

    private val scaleMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)
    private val translateMatrix = FloatArray(16)

    private fun setIdentities() {
        Matrix.setIdentityM(scaleMatrix, 0)
        Matrix.setIdentityM(rotationMatrix, 0)
        Matrix.setIdentityM(translateMatrix, 0)
    }

    open fun setMatrix(matrix: Matrix4f) {
        this.mvpMatrix.elements = matrix.elements.clone()

        setIdentities()
        Matrix.translateM(translateMatrix, 0, translateX, translateY, 0f)
        Matrix.multiplyMM(
            mvpMatrix.elements, 0, mvpMatrix.elements, 0,
            translateMatrix, 0
        )
    }

    override fun writeUniforms() {
        super.writeUniforms()
        alphaUniform.setValue(alpha)
    }

    companion object {

        // language=string
        private const val ALPHA_UNIFORM = "alpha"

        // language=glsl
        @JvmStatic
        private val OVERLAY_FRAGMENT_SHADER = """
            precision mediump float;
            uniform sampler2D texture;
            uniform float $ALPHA_UNIFORM;
            varying vec2 texCoord;

            void main() {
                vec4 textureColor = texture2D(texture, texCoord);
                gl_FragColor = mix(vec4(0.0), textureColor, $ALPHA_UNIFORM);
            }
        """.trimIndent()
    }
}
