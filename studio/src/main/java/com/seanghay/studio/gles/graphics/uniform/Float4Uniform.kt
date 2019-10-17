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
import android.opengl.GLES20.glUniform4f
import com.seanghay.studio.gles.graphics.Vector4f

fun uniform4f(name: String) = Float4Uniform(name)

class Float4Uniform(name: String) : Uniform<Vector4f>(name) {

    override fun setValue(value: Vector4f) {
        rationalChecks()
        glUniform4f(getLocation(), value.x, value.y, value.z, value.a)
        cachedValue = value
    }

    override fun getValue(): Vector4f {
        rationalChecks()
        val args = FloatArray(4)
        glGetUniformfv(program, getLocation(), args, 0)
        return Vector4f.fromArray(args)
    }
}
