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

import android.opengl.GLES20
import android.opengl.GLES20.GL_TRIANGLE_STRIP
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.egl.glScope
import com.seanghay.studio.gles.graphics.Matrix4f
import com.seanghay.studio.gles.graphics.attribute.VertexAttribute
import com.seanghay.studio.gles.graphics.mat4
import com.seanghay.studio.gles.graphics.texture.Texture
import com.seanghay.studio.gles.graphics.uniform.BooleanUniform
import com.seanghay.studio.gles.graphics.uniform.Mat4Uniform
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform1i
import com.seanghay.studio.gles.graphics.uniform.uniform2f
import com.seanghay.studio.gles.graphics.vec2
import com.seanghay.studio.utils.BasicVertices

open class TextureShader(
  override var vertexShaderSource: String = VERTEX_SHADER,
  override var fragmentShaderSource: String = FRAGMENT_SHADER,
  var coords: FloatArray = BasicVertices.FULL_RECTANGLE
) : Shader() {

    var hasExternalTexture = false

    var isFlipVertical: Boolean = false
    var isFlipHorizontal: Boolean = false
    var mvpMatrix: Matrix4f = mat4()

    // Default to square ratio 1:1
    var ratio: Float = 1.0f

    // Viewport
    var resolution = vec2(0f, 0f)

    open var textureCoordinateAttr = VertexAttribute(
        "vTextureCoordinate",
        BasicVertices.NORMAL_TEXTURE_COORDINATES, 2
    ).autoInit()

    open var positionAttr = VertexAttribute(
        "vPosition",
        coords, 3
    ).autoInit()
        protected set

    open var flipVerticalUniform = BooleanUniform("flipY").autoInit()
        protected set

    open var flipHorizontalUniform = BooleanUniform("flipX").autoInit()
        protected set

    open var mvpMatrixUniform = Mat4Uniform("mvpMatrix").autoInit()
        protected set

    open var ratioUniform = uniform1f("ratio")
        .also { it.isOptional = true }
        .autoInit()

    open var resolutionUniform = uniform2f("resolution")
        .also { it.isOptional = true }
        .autoInit()

    open var textureUniform = uniform1i("texture").autoInit()

    override fun loadFragmentShaderSource(): String {
        return transformFragmentShader(fragmentShaderSource)
    }

    @GlContext
    fun updatePositionAttr() {
        positionAttr = VertexAttribute(
            "vPosition",
            coords, 3
        )

        glScope {
            positionAttr.initialize(program)
        }
    }

    // Noop
    override fun onCreate() {}

    open fun beforeDraw() {}
    open fun afterDraw() {}
    open fun beforeDrawVertices() {}
    open fun afterDrawVertices() {}

    protected open fun writeUniforms() {
        flipVerticalUniform.setValue(isFlipVertical)
        flipHorizontalUniform.setValue(isFlipHorizontal)
        mvpMatrixUniform.setValue(mvpMatrix)
        ratioUniform.setValue(ratio)
        resolutionUniform.setValue(resolution)
        textureUniform.setValue(5)
    }

    open fun setResolution(width: Float, height: Float) {
        resolution.x = width
        resolution.y = height
    }

    open fun draw(texture: Texture) = use {
        positionAttr.use {
            beforeDraw()
            textureCoordinateAttr.use {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE5)
                texture.use(GLES20.GL_TEXTURE_2D) {
                    writeUniforms()
                    beforeDrawVertices()
                    drawArrays(GL_TRIANGLE_STRIP)
                    afterDrawVertices()
                }
            }

            afterDraw()
        }
    }

    fun transformFragmentShader(frag: String): String {
        var str = frag
        if (hasExternalTexture) {
            str = "#extension GL_OES_EGL_image_external : require\n$str"
                .replace("sampler2D", "samplerExternalOES")
        }
        return str
    }

    companion object {

        // language=glsl
        const val VERTEX_SHADER = """
            precision mediump float;
            
            attribute vec4 vPosition;
            attribute vec2 vTextureCoordinate;

            varying vec2 texCoord;

            uniform mat4 mvpMatrix;
            uniform bool flipY;
            uniform bool flipX;
            uniform float ratio;

            void main() {
                gl_Position = mvpMatrix * vPosition;
                texCoord = vTextureCoordinate;

                if (flipY) {
                    texCoord.y = 1.0 - texCoord.y;
                }

                if (flipX) {
                    texCoord.x = 1.0 - texCoord.x;
                }
            }
        """

        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;

            uniform sampler2D texture;

            varying vec2 texCoord;

            void main() {
                gl_FragColor = texture2D(texture, texCoord);
            }
        """
    }
}
