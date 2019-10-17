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

fun uniform1f(name: String) = FloatUniform(name)

class FloatUniform(name: String) : Uniform<Float>(name) {

    override fun setValue(value: Float) {
        rationalChecks()
        if (cachedValue == value) return
        GLES20.glUniform1f(getLocation(), value)
        cachedValue = value
    }

    override fun getValue(): Float {
        rationalChecks()
        val args = FloatArray(1)
        GLES20.glGetUniformfv(program, getLocation(), args, 0)
        return args[0]
    }
}
