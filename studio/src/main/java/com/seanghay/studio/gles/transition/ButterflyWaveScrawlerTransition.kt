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

open class ButterflyWaveScrawlerTransition : Transition("butterfly-wave-scrawler", SOURCE, 1000L) {

    open var amplitude: Float = 1f
    open var amplitudeUniform = uniform1f("amplitude").autoInit()
    open var waves: Float = 30f
    open var wavesUniform = uniform1f("waves").autoInit()
    open var colorSeparation: Float = 0.3f
    open var colorSeparationUniform = uniform1f("colorSeparation").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        amplitudeUniform.setValue(amplitude)
        wavesUniform.setValue(waves)
        colorSeparationUniform.setValue(colorSeparation)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: mandubian
// License: MIT
uniform float amplitude; // = 1.0
uniform float waves; // = 30.0
uniform float colorSeparation; // = 0.3
float PI = 3.14159265358979323846264;
float compute(vec2 p, float progress, vec2 center) {
vec2 o = p*sin(progress * amplitude)-center;
// horizontal vector
vec2 h = vec2(1., 0.);
// butterfly polar function (don't ask me why this one :))
float theta = acos(dot(o, h)) * waves;
return (exp(cos(theta)) - 2.*cos(4.*theta) + pow(sin((2.*theta - PI) / 24.), 5.)) / 10.;
}
vec4 transition(vec2 uv) {
  vec2 p = uv.xy / vec2(1.0).xy;
  float inv = 1. - progress;
  vec2 dir = p - vec2(.5);
  float dist = length(dir);
  float disp = compute(p, progress, vec2(0.5, 0.5)) ;
  vec4 texTo = getToColor(p + inv*disp);
  vec4 texFrom = vec4(
  getFromColor(p + progress*disp*(1.0 - colorSeparation)).r,
  getFromColor(p + progress*disp).g,
  getFromColor(p + progress*disp*(1.0 + colorSeparation)).b,
  1.0);
  return texTo*progress + texFrom*inv;
}

        """
    }
}
