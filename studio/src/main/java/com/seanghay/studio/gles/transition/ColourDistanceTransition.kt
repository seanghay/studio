package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.uniform1f

class ColourDistanceTransition : Transition("colour-distance", SOURCE, 1000L) {

    open var power: Float = 5f
    open var powerUniform = uniform1f("power").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        powerUniform.setValue(power)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// License: MIT
// Author: P-Seebauer
// ported by gre from https://gist.github.com/P-Seebauer/2a5fa2f77c883dd661f9

uniform float power; // = 5.0

vec4 transition(vec2 p) {
  vec4 fTex = getFromColor(p);
  vec4 tTex = getToColor(p);
  float m = step(distance(fTex, tTex), progress);
  return mix(
    mix(fTex, tTex, m),
    tTex,
    pow(progress, power)
  );
}

        """
    }
}


