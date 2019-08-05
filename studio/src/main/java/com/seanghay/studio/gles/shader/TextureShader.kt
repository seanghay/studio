package com.seanghay.studio.gles.shader

import android.opengl.GLES20
import com.seanghay.studio.gles.graphics.Matrix4f
import com.seanghay.studio.gles.graphics.attribute.VertexAttribute
import com.seanghay.studio.gles.graphics.mat4
import com.seanghay.studio.gles.graphics.texture.Texture
import com.seanghay.studio.gles.graphics.vec2
import com.seanghay.studio.utils.BasicVertices
import com.seanghay.studio.gles.graphics.uniform.*
import com.seanghay.studio.gles.graphics.uniform.uniform1i

open class TextureShader(override var vertexShaderSource: String = VERTEX_SHADER,
                         override var fragmentShaderSource: String = FRAGMENT_SHADER,
                         var coords: FloatArray = BasicVertices.FULL_RECTANGLE) : Shader() {

    var hasExternalTexture = false

    var isFlipVertical: Boolean = false
    var isFlipHorizontal: Boolean = false
    var mvpMatrix: Matrix4f = mat4()

    // Default to square ratio 1:1
    var ratio: Float = 1.0f

    // Viewport
    var resolution = vec2(0f, 0f)

    open var textureCoordinateAttr = VertexAttribute("vTextureCoordinate",
        BasicVertices.NORMAL_TEXTURE_COORDINATES, 2
    ).autoInit()

    open var positionAttr = VertexAttribute("vPosition",
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

    // Noop
    override fun onCreate() {}
    open fun beforeDraw() {}
    open fun afterDraw() {}
    open fun beforeDrawVertices() {}
    open fun afterDrawVertices() {}

    protected fun writeUniforms() {
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
                    drawArrays()
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
