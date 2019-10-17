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
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform2f

class PolkaDotsCurtainTransition : Transition("polka-dots-curtain", SOURCE, 1000L) {

    open var dots: Float = 20f
    open var dotsUniform = uniform1f("dots").autoInit()
    open var center: Vector2f = Vector2f(0f, 0f)
    open var centerUniform = uniform2f("center").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        dotsUniform.setValue(dots)
        centerUniform.setValue(center)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: bobylito
// license: MIT
const float SQRT_2 = 1.414213562373;
uniform float dots;// = 20.0;
uniform vec2 center;// = vec2(0, 0);

vec4 transition(vec2 uv) {
  bool nextImage = distance(fract(uv * dots), vec2(0.5, 0.5)) < ( progress / distance(uv, center));
  return nextImage ? getToColor(uv) : getFromColor(uv);
}

        """
    }
}
