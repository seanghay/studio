package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.Vector2f
import com.seanghay.studio.gles.graphics.Vector3f
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform2f
import com.seanghay.studio.gles.graphics.uniform.uniform3f

class UndulatingBurnOutTransition : Transition("undulating-burn-out", SOURCE, 1000L) {

    open var smoothness: Float = 0.03f
    open var smoothnessUniform = uniform1f("smoothness").autoInit()
    open var center: Vector2f = Vector2f(0.5f, 0.5f)
    open var centerUniform = uniform2f("center").autoInit()
    open var color: Vector3f = Vector3f(0f, 0f, 0f)
    open var colorUniform = uniform3f("color").autoInit()


    override fun onUpdateUniforms() {
        super.onUpdateUniforms()

        smoothnessUniform.setValue(smoothness)
        centerUniform.setValue(center)
        colorUniform.setValue(color)
    }

    companion object {
        // language=glsl
        const val SOURCE = """
// License: MIT
// Author: pthrasher
// adapted by gre from https://gist.github.com/pthrasher/8e6226b215548ba12734

uniform float smoothness; // = 0.03
uniform vec2 center; // = vec2(0.5)
uniform vec3 color; // = vec3(0.0)

const float M_PI = 3.14159265358979323846;

float quadraticInOut(float t) {
  float p = 2.0 * t * t;
  return t < 0.5 ? p : -p + (4.0 * t) - 1.0;
}

float getGradient(float r, float dist) {
  float d = r - dist;
  return mix(
    smoothstep(-smoothness, 0.0, r - dist * (1.0 + smoothness)),
    -1.0 - step(0.005, d),
    step(-0.005, d) * step(d, 0.01)
  );
}

float getWave(vec2 p){
  vec2 _p = p - center; // offset from center
  float rads = atan(_p.y, _p.x);
  float degs = degrees(rads) + 180.0;
  vec2 range = vec2(0.0, M_PI * 30.0);
  vec2 domain = vec2(0.0, 360.0);
  float ratio = (M_PI * 30.0) / 360.0;
  degs = degs * ratio;
  float x = progress;
  float magnitude = mix(0.02, 0.09, smoothstep(0.0, 1.0, x));
  float offset = mix(40.0, 30.0, smoothstep(0.0, 1.0, x));
  float ease_degs = quadraticInOut(sin(degs));
  float deg_wave_pos = (ease_degs * magnitude) * sin(x * offset);
  return x + deg_wave_pos;
}

vec4 transition(vec2 p) {
  float dist = distance(center, p);
  float m = getGradient(getWave(p), dist);
  vec4 cfrom = getFromColor(p);
  vec4 cto = getToColor(p);
  return mix(mix(cfrom, cto, m), mix(cfrom, vec4(color, 1.0), 0.75), step(m, -2.0));
}

        """
    }
}


