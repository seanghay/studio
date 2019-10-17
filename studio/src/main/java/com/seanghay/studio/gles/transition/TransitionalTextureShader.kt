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
package com.seanghay.studio.gles.transition

import android.opengl.GLES20.GL_TEXTURE1
import android.opengl.GLES20.GL_TEXTURE2
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glActiveTexture
import com.seanghay.studio.gles.graphics.texture.Texture
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform1i
import com.seanghay.studio.gles.shader.TextureShader
import java.util.LinkedList
import java.util.Queue

open class TransitionalTextureShader(var transition: Transition) : TextureShader() {

    override var fragmentShaderSource: String = FRAGMENT_SHADER mergeWith transition.source

    var progress = 0f

    var progressUniform = uniform1f("progress").autoInit()
    var textureFromUniform = uniform1i("texture").autoInit()
    var textureToUniform = uniform1i("texture2").autoInit()

    var runOnPreDraw: Queue<Runnable> = LinkedList()

    init {
        transition.uniforms.forEach {
            it.autoInit()
        }
    }

    override fun beforeDrawVertices() {
        super.beforeDrawVertices()

        transition.onUpdateUniforms()
        progressUniform.setValue(progress)
        textureFromUniform.setValue(1)
        textureToUniform.setValue(2)
    }

    // Don't forget to use the program
    open fun draw(tex1: Texture, tex2: Texture) = use {

        while (runOnPreDraw.isNotEmpty()) {
            runOnPreDraw.poll()?.run()
        }

        positionAttr.use {
            beforeDraw()
            textureCoordinateAttr.use {
                glActiveTexture(GL_TEXTURE1)
                tex1.use(GL_TEXTURE_2D) {

                    glActiveTexture(GL_TEXTURE2)
                    tex2.use(GL_TEXTURE_2D) {

                        writeUniforms()
                        beforeDrawVertices()

                        drawArrays()
                        afterDrawVertices()
                    }
                }
            }

            afterDraw()
        }
    }

    private inline infix fun String.mergeWith(b: String): String {
        return "$this\n$b"
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;

            uniform sampler2D texture;
            uniform sampler2D texture2;
            uniform sampler2D textureQuote;

            uniform float progress;
            uniform float ratio;

            varying vec2 texCoord;

            vec4 getFromColor(vec2 uv) {
                return texture2D(texture, uv);
            }

            vec4 getToColor(vec2 uv) {
                return texture2D(texture2, uv);
            }

            vec4 transition(vec2 uv);

            void main() {
                gl_FragColor = transition(texCoord);
            }
        """
    }
}
