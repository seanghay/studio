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
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform3f

open class FadecolorTransition : Transition("fadecolor", SOURCE, 1000L) {

    open var color: Vector3f = Vector3f(0f, 0f, 0f)
    open var colorUniform = uniform3f("color").autoInit()
    open var colorPhase: Float = 0.4f
    open var colorPhaseUniform = uniform1f("colorPhase").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        colorUniform.setValue(color)
        colorPhaseUniform.setValue(colorPhase)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: gre
// License: MIT
uniform vec3 color;// = vec3(0.0)
uniform float colorPhase/* = 0.4 */; // if 0.0, there is no black phase, if 0.9, the black phase is very important
vec4 transition (vec2 uv) {
  return mix(
    mix(vec4(color, 1.0), getFromColor(uv), smoothstep(1.0-colorPhase, 0.0, progress)),
    mix(vec4(color, 1.0), getToColor(uv), smoothstep(    colorPhase, 1.0, progress)),
    progress);
}

        """
    }
}
