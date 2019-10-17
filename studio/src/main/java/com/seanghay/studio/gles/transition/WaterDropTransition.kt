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

class WaterDropTransition : Transition("water-drop", SOURCE, 1000L) {

    open var amplitude: Float = 30f
    open var amplitudeUniform = uniform1f("amplitude").autoInit()
    open var speed: Float = 30f
    open var speedUniform = uniform1f("speed").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        amplitudeUniform.setValue(amplitude)
        speedUniform.setValue(speed)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: Paweł Płóciennik
// license: MIT
uniform float amplitude; // = 30
uniform float speed; // = 30

vec4 transition(vec2 p) {
  vec2 dir = p - vec2(.5);
  float dist = length(dir);

  if (dist > progress) {
    return mix(getFromColor( p), getToColor( p), progress);
  } else {
    vec2 offset = dir * sin(dist * amplitude - progress * speed);
    return mix(getFromColor( p + offset), getToColor( p), progress);
  }
}

        """
    }
}
