package com.seanghay.studioexample.experiment.core

import android.opengl.Matrix


// Matrices
object ProjectionUtils {

    fun times(result: FloatArray, a: FloatArray, b: FloatArray): FloatArray {
        Matrix.multiplyMM(result, 0, a, 0, b, 0)
        return result
    }

    fun identities(vararg elements: FloatArray) {
        elements.forEach {
            identity(it)
        }
    }

    fun identity(elements: FloatArray) {
        Matrix.setIdentityM(elements, 0)
    }


    fun applyLookAt(elements: FloatArray) {
        Matrix.setLookAtM(
            elements,
            0,
            0f,
            0f,
            -3f,
            0f,
            0f,
            0f,
            0f,
            1.0f,
            0.0f
        )
    }

    fun applyAspectRatio(elements: FloatArray, ratio: Float) {
        Matrix.frustumM(elements, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }
}