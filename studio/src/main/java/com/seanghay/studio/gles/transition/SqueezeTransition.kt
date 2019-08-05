package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.uniform1f

class SqueezeTransition : Transition("squeeze", SOURCE, 1000L) {

    open var colorSeparation: Float = 0.04f
    open var colorSeparationUniform = uniform1f("colorSeparation").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        colorSeparationUniform.setValue(colorSeparation)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: gre
// License: MIT
 
uniform float colorSeparation; // = 0.04
 
vec4 transition (vec2 uv) {
  float y = 0.5 + (uv.y-0.5) / (1.0-progress);
  if (y < 0.0 || y > 1.0) {
     return getToColor(uv);
  }
  else {
    vec2 fp = vec2(uv.x, y);
    vec2 off = progress * vec2(0.0, colorSeparation);
    vec4 c = getFromColor(fp);
    vec4 cn = getFromColor(fp - off);
    vec4 cp = getFromColor(fp + off);
    return vec4(cn.r, c.g, cp.b, c.a);
  }
}

        """
    }
}


