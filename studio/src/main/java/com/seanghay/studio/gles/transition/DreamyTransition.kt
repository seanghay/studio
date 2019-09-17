package com.seanghay.studio.gles.transition

class DreamyTransition : Transition("dreamy", SOURCE, 1000L) {

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: mikolalysenko
// License: MIT

vec2 offset(float progress, float x, float theta) {
  float phase = progress*progress + progress + theta;
  float shifty = 0.03*progress*cos(10.0*(progress+x));
  return vec2(0, shifty);
}
vec4 transition(vec2 p) {
  return mix(getFromColor(p + offset(progress, p.x, 0.0)), getToColor(p + offset(1.0-progress, p.x, 3.14)), progress);
}

        """
    }
}


