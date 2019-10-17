/**
 * Designed and developed by Seanghay Yath (@seanghay)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    fun cachedValue(): T = cachedValue ?: throw RuntimeException("cachedValue was null")
}
