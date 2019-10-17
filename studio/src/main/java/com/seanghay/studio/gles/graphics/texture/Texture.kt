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
package com.seanghay.studio.gles.graphics.texture

import android.opengl.GLES20
import com.seanghay.studio.gles.annotation.GlContext

open class Texture(var id: Int = NO_TEXTURE) {

    @GlContext
    open fun initialize(force: Boolean = false) {
        if (id == NO_TEXTURE || force) {
            createTexture()
        }
    }

    @GlContext
    open fun createTexture() {
        val args = IntArray(1)
        GLES20.glGenTextures(args.size, args, 0)
        id = args[0]
    }

    @GlContext
    open fun enable(textureTarget: Int) {
        GLES20.glBindTexture(textureTarget, id)
    }

    @GlContext
    open fun disable(textureTarget: Int) {
        GLES20.glBindTexture(textureTarget, 0)
    }

    inline fun use(textureTarget: Int, block: () -> Unit) {
        enable(textureTarget)
        block()
        disable(textureTarget)
    }

    fun release() {
        if (id == NO_TEXTURE) return
        val args = intArrayOf(id)
        GLES20.glDeleteTextures(1, args, 0)
    }

    companion object {
        const val NO_TEXTURE = -1
    }
}
