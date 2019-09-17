package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.uniform1f

class RippleTransition : Transition("ripple", SOURCE, 2000) {

    var speed = 50.0f
    var amplitude = 100.0f


    private val amplitudeUniform = uniform1f("amplitude")
        .autoInit()

    private val speedUniform = uniform1f("speed")
        .autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        amplitudeUniform.setValue(amplitude)
        speedUniform.setValue(speed)

    }

    companion object {
        // language=glsl
        const val SOURCE = """
            uniform float amplitude; // = 100.0
            uniform float speed; // = 50.0
            
            vec4 transition (vec2 uv) {
              vec2 dir = uv - vec2(.5);
              float dist = length(dir);
              vec2 offset = dir * (sin(progress * dist * amplitude - progress * speed) + .5) / 30.;
              return mix(
                getFromColor(uv + offset),
                getToColor(uv),
                smoothstep(0.2, 1.0, progress)
              );
            } 
        """
    }
}