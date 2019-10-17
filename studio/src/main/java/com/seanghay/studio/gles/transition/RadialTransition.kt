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

class RadialTransition : Transition("radial", SOURCE, 1000L) {

    open var smoothness: Float = 1f
    open var smoothnessUniform = uniform1f("smoothness").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        smoothnessUniform.setValue(smoothness)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// License: MIT
// Author: Xaychru
// ported by gre from https://gist.github.com/Xaychru/ce1d48f0ce00bb379750

uniform float smoothness; // = 1.0

const float PI = 3.141592653589;

vec4 transition(vec2 p) {
  vec2 rp = p*2.-1.;
  return mix(
    getToColor(p),
    getFromColor(p),
    smoothstep(0., smoothness, atan(rp.y,rp.x) - (progress-.5) * PI * 2.5)
  );
}

        """
    }
}
