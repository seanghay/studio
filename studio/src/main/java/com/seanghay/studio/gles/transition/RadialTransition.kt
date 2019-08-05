package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.gles.graphics.uniform.uniform1f

class RadialTransition : Transition("radial", SOURCE, 1000L) {

    open var smoothness: Float = 1f
    open var smoothnessUniform = uniform1f("smoothness").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        smoothnessUniform.setValue(smoothness)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// License: MIT
// Author: Xaychru
// ported by gre from https://gist.github.com/Xaychru/ce1d48f0ce00bb379750

uniform float smoothness; // = 1.0

const float PI = 3.141592653589;

vec4 transition(vec2 p) {
  vec2 rp = p*2.-1.;
  return mix(
    getToColor(p),
    getFromColor(p),
    smoothstep(0., smoothness, atan(rp.y,rp.x) - (progress-.5) * PI * 2.5)
  );
}

        """
    }
}


