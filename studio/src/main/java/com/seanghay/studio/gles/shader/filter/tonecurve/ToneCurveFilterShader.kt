package com.seanghay.studio.gles.shader.filter.tonecurve

import android.graphics.PointF
import android.opengl.GLES20.*
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.egl.glScope
import com.seanghay.studio.gles.graphics.texture.Texture2d
import com.seanghay.studio.gles.graphics.uniform.uniform1i
import com.seanghay.studio.gles.shader.TextureShader
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




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


    fun setRgbCompositeControlPoints(points: Array<PointF>) {
        rgbCompositeControlPoints = points
        rgbCompositeCurve = createSplineCurve(rgbCompositeControlPoints)
            ?: throw RuntimeException("Points was null")

        updateToneCurveTexture()
    }

    fun setRedControlPoints(points: Array<PointF>) {
        redControlPoints = points
        redCurve = createSplineCurve(redControlPoints)
            ?: throw RuntimeException("redControlPoints was null")
        updateToneCurveTexture()
    }

    fun setGreenControlPoints(points: Array<PointF>) {
        greenControlPoints = points
        greenCurve =
            createSplineCurve(greenControlPoints)
                ?: throw RuntimeException("greenControlPoints was null")
        updateToneCurveTexture()
    }

    fun setBlueControlPoints(points: Array<PointF>) {
        blueControlPoints = points
        blueCurve =
            createSplineCurve(blueControlPoints)
                ?: throw RuntimeException("blueControlPoints was null")
        updateToneCurveTexture()
    }

    fun updateToneCurveTexture() {
        glScope("Update ToneCurve Texture") {
            preDraw(this::updateToneCurve)
        }
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
            
            void main() {
                lowp vec4 t = texture2D(texture, texCoord);
                lowp float r = texture2D(curveTexture, vec2(t.r, 0.0)).r;
                lowp float g = texture2D(curveTexture, vec2(t.g, 0.0)).g;
                lowp float b = texture2D(curveTexture, vec2(t.b, 0.0)).b;
                gl_FragColor = vec4(r, g, b, t.a);
            }
            
        """.trimIndent()
    }
}