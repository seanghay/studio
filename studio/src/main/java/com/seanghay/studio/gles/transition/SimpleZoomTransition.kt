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

open class SimpleZoomTransition : Transition("simple-zoom", SOURCE, 1000L) {

    open var zoomQuickness: Float = 0.8f
    open var zoomQuicknessUniform = uniform1f("zoom_quickness").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        zoomQuicknessUniform.setValue(zoomQuickness)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: 0gust1
// License: MIT

uniform float zoom_quickness; // = 0.8


vec2 zoom(vec2 uv, float amount) {
  return 0.5 + ((uv - 0.5) * (1.0-amount));
}

vec4 transition (vec2 uv) {
  float nQuick = clamp(zoom_quickness,0.2,1.0);

  return mix(
    getFromColor(zoom(uv, smoothstep(0.0, nQuick, progress))),
    getToColor(uv),
   smoothstep(nQuick-0.2, 1.0, progress)
  );
}
        """
    }
}
