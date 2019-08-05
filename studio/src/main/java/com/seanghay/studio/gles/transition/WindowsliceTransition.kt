package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.uniform1f

class WindowsliceTransition : Transition("windowslice", SOURCE, 1000L) {

    open var count: Float = 10f
    open var countUniform = uniform1f("count").autoInit()
    open var smoothness: Float = 0.5f
    open var smoothnessUniform = uniform1f("smoothness").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        countUniform.setValue(count)
        smoothnessUniform.setValue(smoothness)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: gre
// License: MIT

uniform float count; // = 10.0
uniform float smoothness; // = 0.5

vec4 transition (vec2 p) {
  float pr = smoothstep(-smoothness, 0.0, p.x - progress * (1.0 + smoothness));
  float s = step(pr, fract(count * p.x));
  return mix(getFromColor(p), getToColor(p), s);
}

        """
    }
}


