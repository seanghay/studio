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

import com.seanghay.studio.gles.graphics.Vector2f
import com.seanghay.studio.gles.graphics.uniform.uniform2f

open class DirectionalwarpTransition : Transition("directionalwarp", SOURCE, 1000L) {

    open var direction: Vector2f = Vector2f(-1f, 1f)
    open var directionUniform = uniform2f("direction").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        directionUniform.setValue(direction)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: pschroen
// License: MIT

uniform vec2 direction; // = vec2(-1.0, 1.0)

const float smoothness = 0.5;
const vec2 center = vec2(0.5, 0.5);

vec4 transition (vec2 uv) {
  vec2 v = normalize(direction);
  v /= abs(v.x) + abs(v.y);
  float d = v.x * center.x + v.y * center.y;
  float m = 1.0 - smoothstep(-smoothness, 0.0, v.x * uv.x + v.y * uv.y - (d - 0.5 + progress * (1.0 + smoothness)));
  return mix(getFromColor((uv - 0.5) * (1.0 - m) + 0.5), getToColor((uv - 0.5) * m + 0.5), m);
}

        """
    }
}
