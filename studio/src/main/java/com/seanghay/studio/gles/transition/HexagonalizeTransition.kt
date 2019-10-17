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
import com.seanghay.studio.gles.graphics.uniform.uniform1i

class HexagonalizeTransition : Transition("hexagonalize", SOURCE, 1000L) {

    open var steps: Int = 50
    open var stepsUniform = uniform1i("steps").autoInit()
    open var horizontalHexagons: Float = 20f
    open var horizontalHexagonsUniform = uniform1f("horizontalHexagons").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        stepsUniform.setValue(steps)
        horizontalHexagonsUniform.setValue(horizontalHexagons)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Fernando Kuteken
// License: MIT
// Hexagonal math from: http://www.redblobgames.com/grids/hexagons/

uniform int steps; // = 50;
uniform float horizontalHexagons; //= 20;

struct Hexagon {
  float q;
  float r;
  float s;
};

Hexagon createHexagon(float q, float r){
  Hexagon hex;
  hex.q = q;
  hex.r = r;
  hex.s = -q - r;
  return hex;
}

Hexagon roundHexagon(Hexagon hex){

  float q = floor(hex.q + 0.5);
  float r = floor(hex.r + 0.5);
  float s = floor(hex.s + 0.5);

  float deltaQ = abs(q - hex.q);
  float deltaR = abs(r - hex.r);
  float deltaS = abs(s - hex.s);

  if (deltaQ > deltaR && deltaQ > deltaS)
    q = -r - s;
  else if (deltaR > deltaS)
    r = -q - s;
  else
    s = -q - r;

  return createHexagon(q, r);
}

Hexagon hexagonFromPoint(vec2 point, float size) {

  point.y /= ratio;
  point = (point - 0.5) / size;

  float q = (sqrt(3.0) / 3.0) * point.x + (-1.0 / 3.0) * point.y;
  float r = 0.0 * point.x + 2.0 / 3.0 * point.y;

  Hexagon hex = createHexagon(q, r);
  return roundHexagon(hex);

}

vec2 pointFromHexagon(Hexagon hex, float size) {

  float x = (sqrt(3.0) * hex.q + (sqrt(3.0) / 2.0) * hex.r) * size + 0.5;
  float y = (0.0 * hex.q + (3.0 / 2.0) * hex.r) * size + 0.5;

  return vec2(x, y * ratio);
}

vec4 transition (vec2 uv) {

  float dist = 2.0 * min(progress, 1.0 - progress);
  dist = steps > 0 ? ceil(dist * float(steps)) / float(steps) : dist;

  float size = (sqrt(3.0) / 3.0) * dist / horizontalHexagons;

  vec2 point = dist > 0.0 ? pointFromHexagon(hexagonFromPoint(uv, size), size) : uv;

  return mix(getFromColor(point), getToColor(point), progress);

}

        """
    }
}
