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

import android.opengl.GLES20.glGetUniformfv
import android.opengl.GLES20.glUniformMatrix4fv
import com.seanghay.studio.gles.graphics.Matrix4f

fun uniformMat4(name: String) = Mat4Uniform(name)

class Mat4Uniform(name: String) : Uniform<Matrix4f>(name) {

    override fun setValue(value: Matrix4f) {
        rationalChecks()
        glUniformMatrix4fv(getLocation(), 1, false, value.elements, 0)
        cachedValue = value
    }

    override fun getValue(): Matrix4f {
        rationalChecks()
        val args = FloatArray(4 * 4)
        glGetUniformfv(program, getLocation(), args, 0)
        return Matrix4f(args)
    }
}
