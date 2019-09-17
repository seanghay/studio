package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.BooleanUniform
import com.seanghay.studio.gles.graphics.uniform.uniform1f

class CircleopenTransition : Transition("circleopen", SOURCE, 1000L) {

    open var smoothness: Float = 0.3f
    open var smoothnessUniform = uniform1f("smoothness").autoInit()
    open var opening: Boolean = true
    open var openingUniform = BooleanUniform("opening").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        smoothnessUniform.setValue(smoothness)
        openingUniform.setValue(opening)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: gre
// License: MIT
uniform float smoothness; // = 0.3
uniform bool opening; // = true

const vec2 center = vec2(0.5, 0.5);
const float SQRT_2 = 1.414213562373;

vec4 transition (vec2 uv) {
  float x = opening ? progress : 1.-progress;
  float m = smoothstep(-smoothness, 0.0, SQRT_2*distance(center, uv) - x*(1.+smoothness));
  return mix(getFromColor(uv), getToColor(uv), opening ? 1.-m : m);
}

        """
    }
}


