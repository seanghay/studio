package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.Vector2f
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform2f

class PolkaDotsCurtainTransition : Transition("polka-dots-curtain", SOURCE, 1000L) {

    open var dots: Float = 20f
    open var dotsUniform = uniform1f("dots").autoInit()
    open var center: Vector2f = Vector2f(0f, 0f)
    open var centerUniform = uniform2f("center").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        dotsUniform.setValue(dots)
        centerUniform.setValue(center)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// author: bobylito
// license: MIT
const float SQRT_2 = 1.414213562373;
uniform float dots;// = 20.0;
uniform vec2 center;// = vec2(0, 0);

vec4 transition(vec2 uv) {
  bool nextImage = distance(fract(uv * dots), vec2(0.5, 0.5)) < ( progress / distance(uv, center));
  return nextImage ? getToColor(uv) : getFromColor(uv);
}

        """
    }
}


