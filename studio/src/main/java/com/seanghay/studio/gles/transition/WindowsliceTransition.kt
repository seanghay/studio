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

import com.seanghay.studio.gles.graphics.uniform.uniform1f

open class WindowsliceTransition : Transition("windowslice", SOURCE, 1000L) {

    open var count: Float = 10f
    open var countUniform = uniform1f("count").autoInit()
    open var smoothness: Float = 0.5f
    open var smoothnessUniform = uniform1f("smoothness").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        countUniform.setValue(count)
        smoothnessUniform.setValue(smoothness)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: gre
// License: MIT

uniform float count; // = 10.0
uniform float smoothness; // = 0.5

vec4 transition (vec2 p) {
  float pr = smoothstep(-smoothness, 0.0, p.x - progress * (1.0 + smoothness));
  float s = step(pr, fract(count * p.x));
  return mix(getFromColor(p), getToColor(p), s);
}

        """
    }
}
