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
package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.Vector3f
import com.seanghay.studio.gles.graphics.uniform.uniform3f

class BurnTransition : Transition("burn", SOURCE, 1000L) {

    open var color: Vector3f = Vector3f(0.9f, 0.4f, 0.2f)
    open var colorUniform = uniform3f("color").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        colorUniform.setValue(color)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: gre
// License: MIT
uniform vec3 color /* = vec3(0.9, 0.4, 0.2) */;
vec4 transition (vec2 uv) {
  return mix(
    getFromColor(uv) + vec4(progress*color, 1.0),
    getToColor(uv) + vec4((1.0-progress)*color, 1.0),
    progress
  );
}

        """
    }
}
