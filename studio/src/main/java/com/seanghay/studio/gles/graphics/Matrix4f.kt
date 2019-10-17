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

import android.opengl.Matrix

fun mat4(elements: FloatArray) = Matrix4f(elements)
fun mat4() = Matrix4f()

data class Matrix4f(var elements: FloatArray = FloatArray(4 * 4)) :
    Iterator<Float> by elements.iterator() {

    init {
        Matrix.setIdentityM(elements, 0)
    }

    operator fun get(index: Int) = elements[index]

    operator fun set(index: Int, value: Float) {
        elements[index] = value
    }

    operator fun times(mat4: Matrix4f): Matrix4f {
        val result = Matrix4f()
        Matrix.multiplyMM(
            result.elements, 0, elements,
            0, mat4.elements, 0
        )
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix4f

        if (!elements.contentEquals(other.elements)) return false

        return true
    }

    override fun hashCode(): Int {
        return elements.contentHashCode()
    }
}
