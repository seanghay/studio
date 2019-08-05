package com.seanghay.studio.utils

object BasicVertices {

    val FULL_RECTANGLE = floatArrayOf(
        -1f, -1f, 0f,
        -1f,  1f, 0f,
        1f,  1f, 0f,

        -1f, -1f, 0f,
        1f,  1f, 0f,
        1f,  -1f, 0f
    )

    val NORMAL_TEXTURE_COORDINATES = floatArrayOf(
        0f, 0f,
        0f, 1f,
        1f, 1f,

        0f, 0f,
        1f, 1f,
        1f, 0f
    )
}