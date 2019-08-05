package com.seanghay.studio.gles.graphics.attribute

import android.opengl.GLES20
import com.seanghay.studio.utils.GlUtils.FLOAT_SIZE_BYTES
import com.seanghay.studio.utils.toByteBuffer
import com.seanghay.studio.utils.toFloatBuffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer




class VertexAttribute(name: String, var vertices: FloatArray, var coordinatesPerVertex: Int): Attribute<FloatArray>(name) {

    var buffer: FloatBuffer = vertices.toFloatBuffer()
        protected set

    private var lastIndices: ByteArray? = null
    private var lastIndicesBuffer: ByteBuffer? = null

    override fun setValue(value: FloatArray) {
        cachedValue = value
        buffer = value.toFloatBuffer()
    }

    fun enable() {
        GLES20.glEnableVertexAttribArray(getLocation())
        GLES20.glVertexAttribPointer(getLocation(), coordinatesPerVertex, GLES20.GL_FLOAT,
            false, coordinatesPerVertex * FLOAT_SIZE_BYTES, buffer
        )
    }

    fun drawArrays(mode: Int = DRAW_MODE) {
        GLES20.glDrawArrays(mode, 0, vertices.size / coordinatesPerVertex)
    }

    fun drawElements(mode: Int = DRAW_MODE, count: Int, indices: ByteBuffer) {
        if (count <= 0) throw RuntimeException("Vertices count cannot be lower than 0")
        GLES20.glDrawElements(mode, count, GLES20.GL_UNSIGNED_BYTE, indices)
    }

    fun drawElements(mode: Int = DRAW_MODE, indices: ByteArray) {
        val buffer: ByteBuffer =
            (if (lastIndices?.contentEquals(indices) == true) lastIndicesBuffer
            else null) ?: indices.toByteBuffer()
        drawElements(mode, indices.size, buffer)
    }

    fun drawTriangleElements(vararg indices: Byte) {
        drawElements(indices = indices)
    }

    fun disable() {
        GLES20.glDisableVertexAttribArray(getLocation())
    }


    inline fun use(block: VertexAttribute.() -> Unit) {
        enable()
        block(this)
        disable()
    }

    override fun getValue(): FloatArray {
        return vertices
    }

    companion object {
        const val DRAW_MODE = GLES20.GL_TRIANGLES
    }
}