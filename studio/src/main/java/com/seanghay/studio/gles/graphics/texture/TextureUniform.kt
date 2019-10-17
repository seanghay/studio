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
import com.seanghay.studio.gles.graphics.uniform.Uniform

abstract class TextureUniform(name: String, var texture: Texture) : Uniform<Int>(name) {

    abstract var textureTarget: Int

    private var id: Int
        get() = texture.id
        set(value) {
            texture.id = value
        }

    abstract fun configure()

    override fun initialize(p: Int) {
        super.initialize(p)
        texture.initialize()
        enable()
        configure()
        disable()
    }

    open fun setTextureToSlot(textureSlot: Int = GLES20.GL_TEXTURE0) {
        GLES20.glActiveTexture(textureSlot)
        enable()
        setValue(textureSlot - GLES20.GL_TEXTURE0)
    }

    fun enable() {
        rationalChecks()
        texture.enable(textureTarget)
    }

    fun disable() {
        texture.disable(textureTarget)
    }

    inline fun <reified T : TextureUniform> use(block: T.() -> Unit) {
        enable()
        if (this is T) block(this)
        disable()
    }
}
