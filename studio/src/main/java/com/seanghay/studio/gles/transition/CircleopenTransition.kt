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

import com.seanghay.studio.gles.graphics.uniform.BooleanUniform
import com.seanghay.studio.gles.graphics.uniform.uniform1f

open class CircleopenTransition : Transition("circleopen", SOURCE, 1000L) {

    open var smoothness: Float = 0.3f
    open var smoothnessUniform = uniform1f("smoothness").autoInit()
    open var opening: Boolean = true
    open var openingUniform = BooleanUniform("opening").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        smoothnessUniform.setValue(smoothness)
        openingUniform.setValue(opening)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: gre
// License: MIT
uniform float smoothness; // = 0.3
uniform bool opening; // = true

const vec2 center = vec2(0.5, 0.5);
const float SQRT_2 = 1.414213562373;

vec4 transition (vec2 uv) {
  float x = opening ? progress : 1.-progress;
  float m = smoothstep(-smoothness, 0.0, SQRT_2*distance(center, uv) - x*(1.+smoothness));
  return mix(getFromColor(uv), getToColor(uv), opening ? 1.-m : m);
}

        """
    }
}
