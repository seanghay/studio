package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.uniform1f

class MorphTransition : Transition("morph", SOURCE, 1000L) {

    open var strength: Float = 0.1f
    open var strengthUniform = uniform1f("strength").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        strengthUniform.setValue(strength)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: paniq
// License: MIT
uniform float strength; // = 0.1

vec4 transition(vec2 p) {
  vec4 ca = getFromColor(p);
  vec4 cb = getToColor(p);
  
  vec2 oa = (((ca.rg+ca.b)*0.5)*2.0-1.0);
  vec2 ob = (((cb.rg+cb.b)*0.5)*2.0-1.0);
  vec2 oc = mix(oa,ob,0.5)*strength;
  
  float w0 = progress;
  float w1 = 1.0-w0;
  return mix(getFromColor(p+oc*w0), getToColor(p-oc*w1), progress);
}

        """
    }
}


