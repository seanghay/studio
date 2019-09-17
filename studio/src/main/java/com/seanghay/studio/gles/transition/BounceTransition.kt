package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.Vector4f
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform4f

class BounceTransition : Transition("bounce", SOURCE, 1000L) {

    open var shadowColour: Vector4f = Vector4f(0f, 0f, 0f, 0.6f)
    open var shadowColourUniform = uniform4f("shadow_colour").autoInit()
    open var shadowHeight: Float = 0.075f
    open var shadowHeightUniform = uniform1f("shadow_height").autoInit()
    open var bounces: Float = 3f
    open var bouncesUniform = uniform1f("bounces").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        shadowColourUniform.setValue(shadowColour)
        shadowHeightUniform.setValue(shadowHeight)
        bouncesUniform.setValue(bounces)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Adrian Purser
// License: MIT

uniform vec4 shadow_colour; // = vec4(0.,0.,0.,.6)
uniform float shadow_height; // = 0.075
uniform float bounces; // = 3.0

const float PI = 3.14159265358;

vec4 transition (vec2 uv) {
  float time = progress;
  float stime = sin(time * PI / 2.);
  float phase = time * PI * bounces;
  float y = (abs(cos(phase))) * (1.0 - stime);
  float d = uv.y - y;
  return mix(
    mix(
      getToColor(uv),
      shadow_colour,
      step(d, shadow_height) * (1. - mix(
        ((d / shadow_height) * shadow_colour.a) + (1.0 - shadow_colour.a),
        1.0,
        smoothstep(0.95, 1., progress) // fade-out the shadow at the end
      ))
    ),
    getFromColor(vec2(uv.x, uv.y + (1.0 - y))),
    step(d, 0.0)
  );
}

        """
    }
}


