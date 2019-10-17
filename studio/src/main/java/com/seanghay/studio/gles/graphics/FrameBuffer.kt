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
package com.seanghay.studio.gles.graphics

import android.opengl.GLES20.GL_COLOR_ATTACHMENT0
import android.opengl.GLES20.GL_DEPTH_ATTACHMENT
import android.opengl.GLES20.GL_DEPTH_COMPONENT16
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES20.GL_FRAMEBUFFER_BINDING
import android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_MAX_TEXTURE_SIZE
import android.opengl.GLES20.GL_NEAREST
import android.opengl.GLES20.GL_RENDERBUFFER
import android.opengl.GLES20.GL_RENDERBUFFER_BINDING
import android.opengl.GLES20.GL_RGBA
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_BINDING_2D
import android.opengl.GLES20.GL_UNSIGNED_BYTE
import android.opengl.GLES20.glBindFramebuffer
import android.opengl.GLES20.glBindRenderbuffer
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glCheckFramebufferStatus
import android.opengl.GLES20.glDeleteFramebuffers
import android.opengl.GLES20.glDeleteRenderbuffers
import android.opengl.GLES20.glDeleteTextures
import android.opengl.GLES20.glFramebufferRenderbuffer
import android.opengl.GLES20.glFramebufferTexture2D
import android.opengl.GLES20.glGenFramebuffers
import android.opengl.GLES20.glGenRenderbuffers
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glGetIntegerv
import android.opengl.GLES20.glRenderbufferStorage
import android.opengl.GLES20.glTexImage2D
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.graphics.texture.Texture
import com.seanghay.studio.utils.GlUtils.setupTextureSampler

open class FrameBuffer(width: Int = 0, height: Int = 0) {

    var width: Int = width
        protected set

    var height: Int = height
        protected set

    var frameBufferName: Int = 0
        protected set

    var renderBufferName: Int = 0
        protected set

    var texName: Int = 0
        protected set

    var texture: Texture? = null

    @GlContext
    open fun setup() {
        setup(width, height)
    }

    open fun setup(width: Int, height: Int) {

        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException("Invalid width and height!")
        }

        this.width = width
        this.height = height

        val args = IntArray(1)
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0)

        if (width > args[0] || height > args[0]) {
            throw IllegalArgumentException("Width or height is higher than GL_MAX_RENDER_BUFFER")
        }

        glGetIntegerv(GL_FRAMEBUFFER_BINDING, args, 0)
        val savedFrameBuffer = args[0]

        glGetIntegerv(GL_RENDERBUFFER_BINDING, args, 0)
        val savedRenderBuffer = args[0]

        glGetIntegerv(GL_TEXTURE_BINDING_2D, args, 0)
        val savedTexName = args[0]

        release()

        try {

            glGenFramebuffers(args.size, args, 0)
            frameBufferName = args[0]

            glGenRenderbuffers(args.size, args, 0)
            renderBufferName = args[0]

            glGenTextures(args.size, args, 0)
            texName = args[0]

            glBindFramebuffer(GL_FRAMEBUFFER, frameBufferName)
            glBindRenderbuffer(GL_RENDERBUFFER, renderBufferName)

            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height)
            glFramebufferRenderbuffer(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER,
                renderBufferName
            )

            glBindTexture(GL_TEXTURE_2D, texName)
            setupTextureSampler(GL_TEXTURE_2D, GL_LINEAR, GL_NEAREST)
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width,
                height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                null
            )
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texName, 0)

            val frameBufferStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER)

            if (frameBufferStatus != GL_FRAMEBUFFER_COMPLETE) {
                throw RuntimeException("Failed to initialize framebuffer object: $frameBufferStatus")
            }

            texture = Texture(texName)
        } catch (e: RuntimeException) {
            release()
            throw e
        }

        glBindFramebuffer(GL_FRAMEBUFFER, savedFrameBuffer)
        glBindRenderbuffer(GL_RENDERBUFFER, savedRenderBuffer)
        glBindTexture(GL_TEXTURE_2D, savedTexName)
    }

    /**
     * Switch to our framebuffer object.
     */
    open fun enable() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferName)
    }

    /**
     * This will revert to the default framebuffer object.
     */

    open fun disable() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun use(block: (FrameBuffer) -> Unit) {
        enable()
        block(this)
        disable()
    }

    open fun release() {
        val args = IntArray(1)

        args[0] = texName
        glDeleteTextures(args.size, args, 0)
        texName = 0

        args[0] = renderBufferName
        glDeleteRenderbuffers(args.size, args, 0)
        renderBufferName = 0

        args[0] = frameBufferName
        glDeleteFramebuffers(args.size, args, 0)
        frameBufferName = 0
    }

    fun toTexture(): Texture {
        return texture ?: throw RuntimeException("Texture was null did you setup it yet?")
    }
}
