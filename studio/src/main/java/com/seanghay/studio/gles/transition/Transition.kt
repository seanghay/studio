package com.seanghay.studio.gles.transition

import com.seanghay.studio.gles.graphics.uniform.Uniform

abstract class Transition(
    var name: String,
    var source: String,
    var duration: Long
) {

    val uniforms = arrayListOf<Uniform<*>>()
    protected fun <T, U : Uniform<T>> U.autoInit(): U {
        uniforms.add(this)
        return this
    }

    open fun onUpdateUniforms() {}
}



