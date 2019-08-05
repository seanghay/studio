package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.gles.graphics.uniform.uniform1f

class FlyeyeTransition : Transition("flyeye", SOURCE, 1000L) {

    open var size: Float = 0.04f
    open var sizeUniform = uniform1f("size").autoInit()
    open var zoom: Float = 50f
    open var zoomUniform = uniform1f("zoom").autoInit()
    open var colorSeparation: Float = 0.3f
    open var colorSeparationUniform = uniform1f("colorSeparation").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        sizeUniform.setValue(size)
        zoomUniform.setValue(zoom)
        colorSeparationUniform.setValue(colorSeparation)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: gre
// License: MIT
uniform float size; // = 0.04
uniform float zoom; // = 50.0
uniform float colorSeparation; // = 0.3

vec4 transition(vec2 p) {
  float inv = 1. - progress;
  vec2 disp = size*vec2(cos(zoom*p.x), sin(zoom*p.y));
  vec4 texTo = getToColor(p + inv*disp);
  vec4 texFrom = vec4(
    getFromColor(p + progress*disp*(1.0 - colorSeparation)).r,
    getFromColor(p + progress*disp).g,
    getFromColor(p + progress*disp*(1.0 + colorSeparation)).b,
    1.0);
  return texTo*progress + texFrom*inv;
}

        """
    }
}


