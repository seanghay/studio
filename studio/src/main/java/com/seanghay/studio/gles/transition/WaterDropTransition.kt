package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.uniform1f

class WaterDropTransition : Transition("water-drop", SOURCE, 1000L) {

    open var amplitude: Float = 30f
    open var amplitudeUniform = uniform1f("amplitude").autoInit()
    open var speed: Float = 30f
    open var speedUniform = uniform1f("speed").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        amplitudeUniform.setValue(amplitude)
        speedUniform.setValue(speed)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: Paweł Płóciennik
// license: MIT
uniform float amplitude; // = 30
uniform float speed; // = 30

vec4 transition(vec2 p) {
  vec2 dir = p - vec2(.5);
  float dist = length(dir);

  if (dist > progress) {
    return mix(getFromColor( p), getToColor( p), progress);
  } else {
    vec2 offset = dir * sin(dist * amplitude - progress * speed);
    return mix(getFromColor( p + offset), getToColor( p), progress);
  }
}

        """
    }
}


