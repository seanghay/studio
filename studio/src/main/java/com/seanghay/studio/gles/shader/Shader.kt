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
package com.seanghay.studio.gles.shader

import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDetachShader
import android.opengl.GLES20.glUseProgram
import com.seanghay.studio.gles.egl.glScope
import com.seanghay.studio.gles.graphics.InputValue
import com.seanghay.studio.utils.GlUtils

abstract class Shader {

    abstract var vertexShaderSource: String

    abstract var fragmentShaderSource: String

    var fragmentShader: Int = -1
        protected set

    var vertexShader: Int = -1
        protected set

    var program: Int = -1
        protected set

    var isProgramCreated = false
        protected set

    var inputValues = mutableListOf<InputValue<*>>()
        protected set

    abstract fun onCreate()

    protected fun <T : InputValue<*>> T.autoInit(): T {
        inputValues.add(this)
        return this
    }

    protected open fun loadFragmentShaderSource(): String {
        return fragmentShaderSource
    }

    protected fun createProgram() {
        val shaderInfo =
            GlUtils.createProgramWithShaders(vertexShaderSource, loadFragmentShaderSource())
        fragmentShader = shaderInfo.fragmentShader
        vertexShader = shaderInfo.vertexShader
        program = shaderInfo.program
        isProgramCreated = true
    }

    protected fun isValidProgram(): Boolean = program != -1

    protected fun validateProgram() {
        if (!isValidProgram() || !isProgramCreated) {
            throw RuntimeException("Program is invalid!")
        }
    }

    protected open fun enable() {
        validateProgram()
        glUseProgram(program)
    }

    protected open fun disable() {
        glUseProgram(0)
    }

    protected open fun initializeInputValues() {
        for (input in inputValues) {
            input.initialize(program)
        }
    }

    open fun setup() {
        createProgram()
        initializeInputValues()
        onCreate()
    }

    open fun trySetup(): Boolean {
        if (isProgramCreated) return false
        setup()
        return true
    }

    fun releaseShaders() = glScope {
        glDetachShader(program, vertexShader)
        glDetachShader(program, fragmentShader)
    }

    fun releaseProgram() = glScope {
        releaseShaders()
        glDeleteProgram(program)
        isProgramCreated = false
        program = -1
    }

    open fun release() = glScope {
        releaseProgram()
    }

    fun use(block: () -> Unit) {
        enable()
        block()
        disable()
    }
}
