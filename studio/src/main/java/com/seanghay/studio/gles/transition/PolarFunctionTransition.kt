package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.uniform1i

class PolarFunctionTransition : Transition("polar-function", SOURCE, 1000L) {

    open var segments: Int = 5
    open var segmentsUniform = uniform1i("segments").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        segmentsUniform.setValue(segments)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Fernando Kuteken
// License: MIT

#define PI 3.14159265359

uniform int segments; // = 5;

vec4 transition (vec2 uv) {
  
  float angle = atan(uv.y - 0.5, uv.x - 0.5) - 0.5 * PI;
  float normalized = (angle + 1.5 * PI) * (2.0 * PI);
  
  float radius = (cos(float(segments) * angle) + 4.0) / 4.0;
  float difference = length(uv - vec2(0.5, 0.5));
  
  if (difference > radius * progress)
    return getFromColor(uv);
  else
    return getToColor(uv);
}

        """
    }
}


