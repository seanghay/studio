package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.gles.graphics.Vector2f
import com.seanghay.studio.gles.graphics.uniform.uniform2f

class DirectionalTransition : Transition("directional", SOURCE, 1000L) {

    open var direction: Vector2f = Vector2f(0f, 1f)
    open var directionUniform = uniform2f("direction").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        directionUniform.setValue(direction)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: Gaëtan Renaudeau
// License: MIT

uniform vec2 direction; // = vec2(0.0, 1.0)

vec4 transition (vec2 uv) {
  vec2 p = uv + progress * sign(direction);
  vec2 f = fract(p);
  return mix(
    getToColor(f),
    getFromColor(f),
    step(0.0, p.y) * step(p.y, 1.0) * step(0.0, p.x) * step(p.x, 1.0)
  );
}

        """
    }
}


