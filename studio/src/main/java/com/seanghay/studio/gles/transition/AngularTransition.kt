package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.gles.graphics.uniform.uniform1f

class AngularTransition : Transition("angular", SOURCE, 1000L) {

    open var startingAngle: Float = 90f
    open var startingAngleUniform = uniform1f("startingAngle").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        startingAngleUniform.setValue(startingAngle)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Fernando Kuteken
// License: MIT

#define PI 3.141592653589

uniform float startingAngle; // = 90;

vec4 transition (vec2 uv) {
  
  float offset = startingAngle * PI / 180.0;
  float angle = atan(uv.y - 0.5, uv.x - 0.5) + offset;
  float normalizedAngle = (angle + PI) / (2.0 * PI);
  
  normalizedAngle = normalizedAngle - floor(normalizedAngle);

  return mix(
    getFromColor(uv),
    getToColor(uv),
    step(normalizedAngle, progress)
    );
}

        """
    }
}


