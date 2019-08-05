package com.seanghay.studio.gles.graphics

import com.seanghay.studio.gles.annotation.GlContext

abstract class InputValue<T>(var name: String) {

    protected var cachedValue: T? = null
    protected var program: Int = -1
    protected var _location: Int = -1

    protected abstract fun loadLocation(): Int

    @GlContext
    @Throws(RuntimeException::class)
    open fun initialize(p: Int) {
        if (p == -1) throw RuntimeException("Invalid program")
        this.program = p

        _location = loadLocation()
        rationalChecks()
    }

    @Throws(RuntimeException::class)
    protected abstract fun rationalChecks()

    @GlContext
    abstract fun setValue(value: T)

    @GlContext
    abstract fun getValue(): T

    @Throws(RuntimeException::class)
    fun getLocation(): Int {
        rationalChecks()
        return _location
    }

    fun cachedValue(): T = cachedValue?: throw RuntimeException("cachedValue was null")
}