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

open class Texture2dUniform(name: String, var texture2d: Texture2d) :
    TextureUniform(name, texture2d) {

    override var textureTarget: Int = GLES20.GL_TEXTURE_2D

    override fun configure() {
        texture2d.configure(textureTarget)
    }

    override fun setValue(value: Int) {
        rationalChecks()
        GLES20.glUniform1i(_location, value)
    }

    override fun getValue(): Int {
        val args = IntArray(1)
        GLES20.glGetUniformiv(program, _location, args, 0)
        return args[0]
    }
}
