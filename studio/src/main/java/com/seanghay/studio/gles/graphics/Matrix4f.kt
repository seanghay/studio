package com.seanghay.studio.gles.graphics

import android.opengl.Matrix


internal fun mat4(elements: FloatArray) = Matrix4f(elements)
internal fun mat4() = Matrix4f()

data class Matrix4f(var elements: FloatArray = FloatArray(4 * 4)): Iterator<Float> by elements.iterator() {

    init {
        Matrix.setIdentityM(elements, 0)
    }

    operator fun get(index: Int) = elements[index]

    operator fun set(index: Int, value: Float) {
        elements[index] = value
    }

    operator fun times(mat4: Matrix4f): Matrix4f {
        val result = Matrix4f()
        Matrix.multiplyMM(result.elements, 0, elements,
            0, mat4.elements, 0)
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