package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.gles.graphics.Vector3f
import com.seanghay.studio.gles.graphics.uniform.uniform3f

class BurnTransition : Transition("burn", SOURCE, 1000L) {

    open var color: Vector3f = Vector3f(0.9f, 0.4f, 0.2f)
    open var colorUniform = uniform3f("color").autoInit()

    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        colorUniform.setValue(color)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: gre
// License: MIT
uniform vec3 color /* = vec3(0.9, 0.4, 0.2) */;
vec4 transition (vec2 uv) {
  return mix(
    getFromColor(uv) + vec4(progress*color, 1.0),
    getToColor(uv) + vec4((1.0-progress)*color, 1.0),
    progress
  );
}

        """
    }
}


