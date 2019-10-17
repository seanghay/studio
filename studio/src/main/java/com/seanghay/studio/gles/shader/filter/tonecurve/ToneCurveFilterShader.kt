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
package com.seanghay.studio.gles.shader.filter.tonecurve

import android.graphics.PointF
import android.opengl.GLES20.GL_RGBA
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE19
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_UNSIGNED_BYTE
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glTexImage2D
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.egl.glScope
import com.seanghay.studio.gles.graphics.texture.Texture2d
import com.seanghay.studio.gles.graphics.uniform.uniform1i
import com.seanghay.studio.gles.shader.TextureShader
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.LinkedList
import java.util.Queue

class ToneCurveFilterShader : TextureShader(fragmentShaderSource = TONE_CURVE_FRAGMENT_SHADER),
    ToneCurveUtils {

    private val toneCurveTexture: Texture2d = Texture2d()
    private val toneCurveUniform = uniform1i("curveTexture").autoInit()

    private val preDrawRunnables: Queue<Runnable> = LinkedList()
    private val postDrawRunnables: Queue<Runnable> = LinkedList()

    // Curves
    private val defaultCurvePoints =
        arrayOf(PointF(0.0f, 0.0f), PointF(0.5f, 0.5f), PointF(1.0f, 1.0f))

    private var rgbCompositeControlPoints: Array<PointF>
    private var redControlPoints: Array<PointF>
    private var greenControlPoints: Array<PointF>
    private var blueControlPoints: Array<PointF>

    private lateinit var rgbCompositeCurve: ArrayList<Float>
    private lateinit var redCurve: ArrayList<Float>
    private lateinit var greenCurve: ArrayList<Float>
    private lateinit var blueCurve: ArrayList<Float>

    init {
        rgbCompositeControlPoints = defaultCurvePoints.clone()
        redControlPoints = defaultCurvePoints.clone()
        greenControlPoints = defaultCurvePoints.clone()
        blueControlPoints = defaultCurvePoints.clone()

        setRgbCompositeControlPoints(rgbCompositeControlPoints)
        setRedControlPoints(redControlPoints)
        setGreenControlPoints(greenControlPoints)
        setBlueControlPoints(blueControlPoints)
        updateToneCurveTexture()
    }

    private inline fun preDraw(crossinline runnable: () -> Unit) {
        preDrawRunnables.add(Runnable { runnable() })
    }

    private inline fun postDraw(crossinline runnable: () -> Unit) {
        postDrawRunnables.add(Runnable { runnable() })
    }

    private fun executes(runnables: Queue<Runnable>) {
        while (runnables.isNotEmpty()) runnables.poll()?.run()
    }

    override fun onCreate() {
        toneCurveTexture.initialize()
        toneCurveTexture.use(GL_TEXTURE_2D) {
            toneCurveTexture.configure(GL_TEXTURE_2D)
        }
    }

    override fun beforeDrawVertices() {
        executes(preDrawRunnables)
        glActiveTexture(GL_TEXTURE19)
        toneCurveTexture.enable(GL_TEXTURE_2D)
        toneCurveUniform.setValue(GL_TEXTURE19 - GL_TEXTURE0)
    }

    override fun afterDrawVertices() {
        executes(postDrawRunnables)
        toneCurveTexture.disable(GL_TEXTURE_2D)
    }

    private fun setRgbCompositeControlPoints(points: Array<PointF>) {
        rgbCompositeControlPoints = points
        rgbCompositeCurve = createSplineCurve(rgbCompositeControlPoints)
            ?: throw RuntimeException("Points was null")
    }

    private fun setRedControlPoints(points: Array<PointF>) {
        redControlPoints = points
        redCurve = createSplineCurve(redControlPoints)
            ?: throw RuntimeException("redControlPoints was null")
    }

    private fun setGreenControlPoints(points: Array<PointF>) {
        greenControlPoints = points
        greenCurve =
            createSplineCurve(greenControlPoints)
                ?: throw RuntimeException("greenControlPoints was null")
    }

    private fun setBlueControlPoints(points: Array<PointF>) {
        blueControlPoints = points
        blueCurve =
            createSplineCurve(blueControlPoints)
                ?: throw RuntimeException("blueControlPoints was null")
    }

    fun updateToneCurveTexture() {
        glScope("Update ToneCurve Texture") {
            preDraw(this::updateToneCurve)
        }
    }

    fun applyToneCurve(toneCurve: ToneCurve?) {
        if (toneCurve == null) {
            resetCurves()
            return
        }

        val rgb = toneCurve.rgb ?: defaultCurvePoints.clone()
        val r = toneCurve.r ?: defaultCurvePoints.clone()
        val g = toneCurve.g ?: defaultCurvePoints.clone()
        val b = toneCurve.b ?: defaultCurvePoints.clone()

        setRgbCompositeControlPoints(rgb)
        setRedControlPoints(r)
        setGreenControlPoints(g)
        setBlueControlPoints(b)
        updateToneCurveTexture()
    }

    @GlContext
    private fun updateToneCurve() {
        toneCurveTexture.use(GL_TEXTURE_2D) {
            if (redCurve.size >= 256 && greenCurve.size >= 256 && blueCurve.size >= 256 && rgbCompositeCurve.size >= 256) {
                val curves = ByteArray(256 * 4)
                for (curveIndex in 0..255) {
                    curves[curveIndex * 4 + 2] =
                        ((curveIndex.toFloat() + blueCurve[curveIndex] + rgbCompositeCurve[curveIndex]).coerceAtLeast(
                            0f
                        ).coerceAtMost(255f).toInt() and 0xff).toByte()

                    curves[curveIndex * 4 + 1] =
                        ((curveIndex.toFloat() + greenCurve[curveIndex] +
                                rgbCompositeCurve[curveIndex]).coerceAtLeast(
                            0f
                        ).coerceAtMost(255f).toInt() and 0xff).toByte()

                    curves[curveIndex * 4] =
                        ((curveIndex.toFloat() + redCurve[curveIndex] +
                                rgbCompositeCurve[curveIndex]).coerceAtLeast(
                            0f
                        ).coerceAtMost(255f).toInt() and 0xff).toByte()

                    curves[curveIndex * 4 + 3] = 0xff.toByte()
                }

                glTexImage2D(
                    GL_TEXTURE_2D, 0, GL_RGBA, 256, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                    ByteBuffer.wrap(curves)
                )
            }
        }
    }

    fun resetCurves() {
        this.setRgbCompositeControlPoints(defaultCurvePoints.clone())
        this.setRedControlPoints(defaultCurvePoints.clone())
        this.setGreenControlPoints(defaultCurvePoints.clone())
        this.setBlueControlPoints(defaultCurvePoints.clone())
        updateToneCurveTexture()
    }

    fun fromCurveFile(input: InputStream) {
        try {

            // Hell yeah! Must read
            val version = input.readShort()

            val totalCurves = input.readShort()
            val pointRate = 1.0f / 255.0f
            val curves = arrayListOf<Array<PointF>>()

            for (i in 0 until totalCurves) {
                val pointCount = input.readShort()
                val points = arrayListOf<PointF>()

                for (j in 0 until pointCount) {
                    val y = input.readShort()
                    val x = input.readShort()
                    points.add(PointF(x * pointRate, y * pointRate))
                }

                curves.add(points.toTypedArray())
            }

            input.close()

            curves.getOrNull(0)?.takeIf { it.isNotEmpty() }?.let {
                setRgbCompositeControlPoints(it)
            }

            curves.getOrNull(1)?.takeIf { it.isNotEmpty() }?.let {
                setRedControlPoints(it)
            }

            curves.getOrNull(2)?.takeIf { it.isNotEmpty() }?.let {
                setGreenControlPoints(it)
            }

            curves.getOrNull(3)?.takeIf { it.isNotEmpty() }?.let {
                setBlueControlPoints(it)
            }

            updateToneCurveTexture()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun InputStream.readShort(): Short {
        return (read() shl 8 or read()).toShort()
    }

    companion object {

        // language=glsl
        @JvmField
        val TONE_CURVE_FRAGMENT_SHADER = """
            precision mediump float;
            uniform sampler2D texture;
            uniform sampler2D curveTexture;
            varying vec2 texCoord;

            vec4 tone_curve(sampler2D curve, vec4 t);

            void main() {
                lowp vec4 t = texture2D(texture, texCoord);
                 gl_FragColor = tone_curve(curveTexture, t);
            }

            vec4 tone_curve(sampler2D curve, vec4 t) {
                lowp float r = texture2D(curve, vec2(t.r, 0.0)).r;
                lowp float g = texture2D(curve, vec2(t.g, 0.0)).g;
                lowp float b = texture2D(curve, vec2(t.b, 0.0)).b;
                return vec4(r, g, b, t.a);
            }

        """.trimIndent()
    }
}
