package com.seanghay.studio.gles.transition

class CrosswarpTransition : Transition("crosswarp", SOURCE, 2000) {
    companion object {
        // language=glsl
        const val SOURCE = """
            vec4 transition(vec2 p) {
              float x = progress;
              x=smoothstep(.0,1.0,(x*2.0+p.x-1.0));
              return mix(getFromColor((p-.5)*(1.-x)+.5), getToColor((p-.5)*x+.5), x);
            }
             
        """
    }
}