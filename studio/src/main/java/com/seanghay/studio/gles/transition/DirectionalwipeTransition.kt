package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.gles.graphics.Vector2f
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform2f

class DirectionalwipeTransition : Transition("directionalwipe", SOURCE, 1000L) {

    open var direction: Vector2f = Vector2f(1f, -1f)
    open var directionUniform = uniform2f("direction").autoInit()
    open var smoothness: Float = 0.5f
    open var smoothnessUniform = uniform1f("smoothness").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        directionUniform.setValue(direction)
        smoothnessUniform.setValue(smoothness)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: gre
// License: MIT

uniform vec2 direction; // = vec2(1.0, -1.0)
uniform float smoothness; // = 0.5
 
const vec2 center = vec2(0.5, 0.5);
 
vec4 transition (vec2 uv) {
  vec2 v = normalize(direction);
  v /= abs(v.x)+abs(v.y);
  float d = v.x * center.x + v.y * center.y;
  float m =
    (1.0-step(progress, 0.0)) * // there is something wrong with our formula that makes m not equals 0.0 with progress is 0.0
    (1.0 - smoothstep(-smoothness, 0.0, v.x * uv.x + v.y * uv.y - (d-0.5+progress*(1.+smoothness))));
  return mix(getFromColor(uv), getToColor(uv), m);
}

        """
    }
}


