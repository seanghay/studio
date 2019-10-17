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

class PinwheelTransition : Transition("pinwheel", SOURCE, 1000L) {

    open var speed: Float = 2f
    open var speedUniform = uniform1f("speed").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        speedUniform.setValue(speed)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Mr Speaker
// License: MIT

uniform float speed; // = 2.0;

vec4 transition(vec2 uv) {

  vec2 p = uv.xy / vec2(1.0).xy;

  float circPos = atan(p.y - 0.5, p.x - 0.5) + progress * speed;
  float modPos = mod(circPos, 3.1415 / 4.);
  float signed = sign(progress - modPos);

  return mix(getToColor(p), getFromColor(p), step(signed, 0.5));

}

        """
    }
}
