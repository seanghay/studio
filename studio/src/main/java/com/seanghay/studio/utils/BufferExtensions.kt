package com.seanghay.studio.utils

import com.seanghay.studio.utils.GlUtils.FLOAT_SIZE_BYTES
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

internal fun ByteArray.toByteBuffer(): ByteBuffer {
    return ByteBuffer.allocateDirect(size * FLOAT_SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .put(this).also { it.position(0) }
}

internal fun FloatArray.toFloatBuffer(): FloatBuffer {
    return ByteBuffer.allocateDirect(size * FLOAT_SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(this).also { it.position(0) }
}

internal fun IntArray.toIntBuffer(): IntBuffer {
    return ByteBuffer.allocateDirect(size * FLOAT_SIZE_BYTES)
        .order(ByteOrder.nativeOrder()).asIntBuffer()
        .put(this).also { it.position(0) }
}