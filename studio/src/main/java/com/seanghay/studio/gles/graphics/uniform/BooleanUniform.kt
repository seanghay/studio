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
package com.seanghay.studio.gles.graphics.uniform

import android.opengl.GLES20

class BooleanUniform(name: String) : Uniform<Boolean>(name) {

    override fun setValue(value: Boolean) {
        rationalChecks()
        GLES20.glUniform1i(
            getLocation(),
            if (value) GLES20.GL_TRUE else GLES20.GL_FALSE
        )
    }

    override fun getValue(): Boolean {
        rationalChecks()
        val args = IntArray(1)
        GLES20.glGetUniformiv(program, getLocation(), args, 0)
        return args[0] == GLES20.GL_TRUE
    }
}
