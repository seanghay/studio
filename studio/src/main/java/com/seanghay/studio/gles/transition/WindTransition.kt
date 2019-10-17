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

class WindTransition : Transition("wind", SOURCE, 1000L) {

    open var size: Float = 0.2f
    open var sizeUniform = uniform1f("size").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        sizeUniform.setValue(size)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: gre
// License: MIT

// Custom parameters
uniform float size; // = 0.2

float rand (vec2 co) {
  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec4 transition (vec2 uv) {
  float r = rand(vec2(0, uv.y));
  float m = smoothstep(0.0, -size, uv.x*(1.0-size) + size*r - (progress * (1.0 + size)));
  return mix(
    getFromColor(uv),
    getToColor(uv),
    m
  );
}

        """
    }
}
