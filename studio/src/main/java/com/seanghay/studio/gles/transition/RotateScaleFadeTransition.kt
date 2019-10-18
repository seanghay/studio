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
import com.seanghay.studio.gles.graphics.Vector4f
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform2f
import com.seanghay.studio.gles.graphics.uniform.uniform4f

open class RotateScaleFadeTransition : Transition("rotate-scale-fade", SOURCE, 1000L) {

    open var center: Vector2f = Vector2f(0.5f, 0.5f)
    open var centerUniform = uniform2f("center").autoInit()
    open var rotations: Float = 1f
    open var rotationsUniform = uniform1f("rotations").autoInit()
    open var scale: Float = 8f
    open var scaleUniform = uniform1f("scale").autoInit()
    open var backColor: Vector4f = Vector4f(0.15f, 0.15f, 0.15f, 1f)
    open var backColorUniform = uniform4f("backColor").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        centerUniform.setValue(center)
        rotationsUniform.setValue(rotations)
        scaleUniform.setValue(scale)
        backColorUniform.setValue(backColor)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Fernando Kuteken
// License: MIT

#define PI 3.14159265359

uniform vec2 center; // = vec2(0.5, 0.5);
uniform float rotations; // = 1;
uniform float scale; // = 8;
uniform vec4 backColor; // = vec4(0.15, 0.15, 0.15, 1.0);

vec4 transition (vec2 uv) {

  vec2 difference = uv - center;
  vec2 dir = normalize(difference);
  float dist = length(difference);

  float angle = 2.0 * PI * rotations * progress;

  float c = cos(angle);
  float s = sin(angle);

  float currentScale = mix(scale, 1.0, 2.0 * abs(progress - 0.5));

  vec2 rotatedDir = vec2(dir.x  * c - dir.y * s, dir.x * s + dir.y * c);
  vec2 rotatedUv = center + rotatedDir * dist / currentScale;

  if (rotatedUv.x < 0.0 || rotatedUv.x > 1.0 ||
      rotatedUv.y < 0.0 || rotatedUv.y > 1.0)
    return backColor;

  return mix(getFromColor(rotatedUv), getToColor(rotatedUv), progress);
}

        """
    }
}
