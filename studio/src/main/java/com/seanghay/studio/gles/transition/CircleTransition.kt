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
import com.seanghay.studio.gles.graphics.Vector3f
import com.seanghay.studio.gles.graphics.uniform.uniform2f
import com.seanghay.studio.gles.graphics.uniform.uniform3f

open class CircleTransition : Transition("circle", SOURCE, 1000L) {

    open var center: Vector2f = Vector2f(0.5f, 0.5f)
    open var centerUniform = uniform2f("center").autoInit()
    open var backColor: Vector3f = Vector3f(0.1f, 0.1f, 0.1f)
    open var backColorUniform = uniform3f("backColor").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        centerUniform.setValue(center)
        backColorUniform.setValue(backColor)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Fernando Kuteken
// License: MIT

uniform vec2 center; // = vec2(0.5, 0.5);
uniform vec3 backColor; // = vec3(0.1, 0.1, 0.1);

vec4 transition (vec2 uv) {

  float distance = length(uv - center);
  float radius = sqrt(8.0) * abs(progress - 0.5);

  if (distance > radius) {
    return vec4(backColor, 1.0);
  }
  else {
    if (progress < 0.5) return getFromColor(uv);
    else return getToColor(uv);
  }
}

        """
    }
}
