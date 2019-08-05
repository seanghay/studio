package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.gles.graphics.uniform.uniform1f

class PinwheelTransition : Transition("pinwheel", SOURCE, 1000L) {

    open var speed: Float = 2f
    open var speedUniform = uniform1f("speed").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        speedUniform.setValue(speed)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Mr Speaker
// License: MIT

uniform float speed; // = 2.0;

vec4 transition(vec2 uv) {
  
  vec2 p = uv.xy / vec2(1.0).xy;
  
  float circPos = atan(p.y - 0.5, p.x - 0.5) + progress * speed;
  float modPos = mod(circPos, 3.1415 / 4.);
  float signed = sign(progress - modPos);
  
  return mix(getToColor(p), getFromColor(p), step(signed, 0.5));
  
}

        """
    }
}


