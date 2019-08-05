package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.uniform1f

class WindTransition : Transition("wind", SOURCE, 1000L) {

    open var size: Float = 0.2f
    open var sizeUniform = uniform1f("size").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        sizeUniform.setValue(size)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: gre
// License: MIT

// Custom parameters
uniform float size; // = 0.2

float rand (vec2 co) {
  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec4 transition (vec2 uv) {
  float r = rand(vec2(0, uv.y));
  float m = smoothstep(0.0, -size, uv.x*(1.0-size) + size*r - (progress * (1.0 + size)));
  return mix(
    getFromColor(uv),
    getToColor(uv),
    m
  );
}

        """
    }
}


