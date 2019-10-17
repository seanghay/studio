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
import com.seanghay.studio.gles.graphics.InputValue

abstract class Uniform<T>(name: String) : InputValue<T>(name) {

    // Sometimes, uniforms don't need to be existed.
    var isOptional = false

    override fun loadLocation(): Int {
        return GLES20.glGetUniformLocation(program, name)
    }

    override fun rationalChecks() {
        if (program == -1) throw RuntimeException("Invalid program")
        if (_location == -1 && !isOptional)
            throw RuntimeException("Uniform name: $name is not found! Did you Initialize it yet?")
    }
}
