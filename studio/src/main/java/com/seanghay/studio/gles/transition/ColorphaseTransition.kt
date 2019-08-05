package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.gles.graphics.Vector4f
import com.seanghay.studio.gles.graphics.uniform.uniform4f

class ColorphaseTransition : Transition("colorphase", SOURCE, 1000L) {

    open var fromStep: Vector4f = Vector4f(0f, 0.2f, 0.4f, 0f)
    open var fromStepUniform = uniform4f("fromStep").autoInit()
    open var toStep: Vector4f = Vector4f(0.6f, 0.8f, 1f, 1f)
    open var toStepUniform = uniform4f("toStep").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        fromStepUniform.setValue(fromStep)
        toStepUniform.setValue(toStep)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// Author: gre
// License: MIT

// Usage: fromStep and toStep must be in [0.0, 1.0] range 
// and all(fromStep) must be < all(toStep)

uniform vec4 fromStep; // = vec4(0.0, 0.2, 0.4, 0.0)
uniform vec4 toStep; // = vec4(0.6, 0.8, 1.0, 1.0)

vec4 transition (vec2 uv) {
  vec4 a = getFromColor(uv);
  vec4 b = getToColor(uv);
  return mix(a, b, smoothstep(fromStep, toStep, vec4(progress)));
}

        """
    }
}


