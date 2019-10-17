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

class RippleTransition : Transition("ripple", SOURCE, 2000) {

    var speed = 50.0f
    var amplitude = 100.0f

    private val amplitudeUniform = uniform1f("amplitude")
        .autoInit()

    private val speedUniform = uniform1f("speed")
        .autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        amplitudeUniform.setValue(amplitude)
        speedUniform.setValue(speed)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
            uniform float amplitude; // = 100.0
            uniform float speed; // = 50.0

            vec4 transition (vec2 uv) {
              vec2 dir = uv - vec2(.5);
              float dist = length(dir);
              vec2 offset = dir * (sin(progress * dist * amplitude - progress * speed) + .5) / 30.;
              return mix(
                getFromColor(uv + offset),
                getToColor(uv),
                smoothstep(0.2, 1.0, progress)
              );
            }
        """
    }
}
