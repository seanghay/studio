package com.seanghay.studio.gles.shader.filter

import android.graphics.PointF
import android.opengl.GLES20
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.egl.glScope
import com.seanghay.studio.gles.graphics.mat4
import com.seanghay.studio.gles.graphics.texture.Texture2d
import com.seanghay.studio.gles.graphics.uniform.uniform1f
import com.seanghay.studio.gles.graphics.uniform.uniform1i
import com.seanghay.studio.gles.graphics.uniform.uniformMat4
import com.seanghay.studio.gles.shader.TextureShader
import com.seanghay.studio.gles.shader.filter.pack.PackFilter
import com.seanghay.studio.gles.shader.filter.tonecurve.ToneCurve
import com.seanghay.studio.gles.shader.filter.tonecurve.ToneCurveUtils
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*

open class PackFilterShader : TextureShader(fragmentShaderSource = FRAGMENT_SHADER),
    ToneCurveUtils {

    private val toneCurveTexture: Texture2d = Texture2d()
    private val toneCurveUniform = uniform1i("curveTexture").autoInit()

    private val preDrawRunnables: Queue<Runnable> = LinkedList()
    private val postDrawRunnables: Queue<Runnable> = LinkedList()

    // Curves
    private val defaultCurvePoints = arrayOf(
        PointF(0.0f, 0.0f),
        PointF(0.5f, 0.5f),
        PointF(1.0f, 1.0f)
    )


    private var rgbCompositeControlPoints: Array<PointF>
    private var redControlPoints: Array<PointF>
    private var greenControlPoints: Array<PointF>
    private var blueControlPoints: Array<PointF>

    private lateinit var rgbCompositeCurve: ArrayList<Float>
    private lateinit var redCurve: ArrayList<Float>
    private lateinit var greenCurve: ArrayList<Float>
    private lateinit var blueCurve: ArrayList<Float>

    var colorMatrixIntensity = 1f
    var colorMatrix = mat4()
    var sepiaMatrix = mat4()
    var intensity = 1f
    var brightness = 0f
    var contrast = 1f
    var saturation = .5f
    var warmth = 0f
    var tint = 0f
    var gamma = 0f
    var vibrant = 0f
    var sepia = 0f
    var toneCurve: ToneCurve? = ToneCurve.getDefault()

    var intensityUniform = uniform1f("intensity").autoInit()
    var brightnessUniform = uniform1f("brightness").autoInit()
    var contrastUniform = uniform1f("contrast").autoInit()
    var saturationUniform = uniform1f("saturation").autoInit()
    var warmthUniform = uniform1f("warmth").autoInit()
    var tintUniform = uniform1f("tint").autoInit()
    var gammaUniform = uniform1f("gamma").autoInit()
    var vibrantUniform = uniform1f("vibrant").autoInit()
    var sepiaUniform = uniform1f("sepia").autoInit()
    var sepiaMatrixUniform = uniformMat4("sepiaMatrix").autoInit()
    var colorMatrixUniform = uniformMat4("colorMatrix").autoInit()
    var colorMatrixIntensityUniform = uniform1f("colorMatrixIntensity").autoInit()

    init {
        sepiaMatrix.elements = floatArrayOf(
            0.3588f, 0.7044f, 0.1368f, 0.0f,
            0.2990f, 0.5870f, 0.1140f, 0.0f,
            0.2392f, 0.4696f, 0.0912f, 0.0f,
            0f, 0f, 0f, 1.0f
        )


        rgbCompositeControlPoints = defaultCurvePoints.clone()
        redControlPoints = defaultCurvePoints.clone()
        greenControlPoints = defaultCurvePoints.clone()
        blueControlPoints = defaultCurvePoints.clone()

        setRgbCompositeControlPoints(rgbCompositeControlPoints)
        setRedControlPoints(redControlPoints)
        setGreenControlPoints(greenControlPoints)
        setBlueControlPoints(blueControlPoints)
        preDraw { updateValues() }
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

    fun invalidate() {
        preDraw {
            updateValues()
        }
    }

    override fun onCreate() {
        toneCurveTexture.initialize()
        toneCurveTexture.use(GLES20.GL_TEXTURE_2D) {
            toneCurveTexture.configure(GLES20.GL_TEXTURE_2D)
        }

        updateToneCurveTexture()
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
        this.toneCurve = toneCurve

        if (toneCurve == null) {
            return
        }

        toneCurve.rgb?.let {
            setRgbCompositeControlPoints(it)
        }

        toneCurve.r?.let {
            setRedControlPoints(it)
        }

        toneCurve.g?.let {
            setGreenControlPoints(it)
        }

        toneCurve.b?.let {
            setBlueControlPoints(it)
        }

        updateToneCurveTexture()
    }

    @GlContext
    private fun updateToneCurve() {
        toneCurveTexture.use(GLES20.GL_TEXTURE_2D) {
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

                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    256,
                    1,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
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

    fun applyPackFilter(f: PackFilter, refresh: Boolean = true) {
        intensity = f.intensity
        brightness = f.brightness
        contrast = f.contrast
        saturation = f.saturation
        warmth = f.warmth
        tint = f.tint
        gamma = f.gamma
        vibrant = f.vibrant
        sepia = f.sepia

        if (refresh) {
            f.toneCurve?.let {
                applyToneCurve(it)
            }

            invalidate()
        }
    }

    open fun updateValues() {
        intensityUniform.setValue(intensity)
        brightnessUniform.setValue(brightness)
        contrastUniform.setValue(contrast)
        saturationUniform.setValue(saturation)
        warmthUniform.setValue(warmth)
        tintUniform.setValue(tint)
        gammaUniform.setValue(gamma)
        vibrantUniform.setValue(vibrant)
        sepiaUniform.setValue(sepia)
        sepiaMatrixUniform.setValue(sepiaMatrix)
        colorMatrixUniform.setValue(colorMatrix)
        colorMatrixIntensityUniform.setValue(colorMatrixIntensity)
    }

    override fun beforeDrawVertices() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE19)
        toneCurveTexture.enable(GLES20.GL_TEXTURE_2D)
        toneCurveUniform.setValue(GLES20.GL_TEXTURE19 - GLES20.GL_TEXTURE0)

        executes(preDrawRunnables)
    }

    override fun afterDrawVertices() {
        executes(postDrawRunnables)
        toneCurveTexture.disable(GLES20.GL_TEXTURE_2D)
    }


    companion object {
        // language=glsl

        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

            varying vec2 texCoord;
            uniform sampler2D texture;
            uniform sampler2D curveTexture;
            
            uniform float intensity;
            
            uniform float brightness;
            uniform float contrast;
            uniform float saturation;
            uniform float warmth;
            uniform float tint;
            uniform mat4 colorMatrix;
            uniform float colorMatrixIntensity;
            
            uniform float gamma;
            uniform float vibrant;
            
            uniform float sepia;
            uniform mat4 sepiaMatrix;

            vec4 applyBrightness(vec4 color, float value) {
                color.rgb += clamp(value, 0.0, 1.0);
                return color;
            }
            
            vec4 applyContrast(vec4 color, float value) {
                float a = clamp(value, 0.5, 1.5);
                return vec4(((color.rgb - vec3(0.5)) * a + vec3(0.5)), color.w);
            }

            vec4 applySaturation(vec4 color, float value) {
                float luminance = dot(color.rgb, luminanceWeighting);
                vec3 greyScaleColor = vec3(luminance);
                return vec4(mix(greyScaleColor, color.rgb, value), color.w);
            }
            
            vec4 applyWarmth(vec4 color, float value) { 
                color.r += value;
                color.b -= value;
                return color;
            }
            
            vec4 applyTint(vec4 color, float value) {
                color.g += clamp(value, -0.2, 0.2);
                return color;
            }
            
            vec4 applyGamma(vec4 color, float value) {
                return vec4(pow(color.rgb, vec3(value)), color.w);
            }
            
            vec4 applyVibrant(vec4 color, float value) {
                float mx = max(max(color.r, color.g), color.b);
                float average = (color.r + color.g + color.b) / 3.0;
                float amt = (mx - average) * (-value * 3.0);
                color.rgb = mix(color.rgb, vec3(mx), amt);
                return color;
            }
            
            vec4 applyColorMatrix(vec4 color, mat4 matrix, float opaque) {
                return (color * matrix) * opaque + color * (1.0 - opaque);
            }
            
            vec4 applySepia(vec4 color, float value) {
                return applyColorMatrix(color, sepiaMatrix, value);
            }

            vec4 tone_curve(sampler2D curve, vec4 t) {
                lowp float r = texture2D(curve, vec2(t.r, 0.0)).r;
                lowp float g = texture2D(curve, vec2(t.g, 0.0)).g;
                lowp float b = texture2D(curve, vec2(t.b, 0.0)).b;
                return vec4(r, g, b, t.a);
            }
            
            
            void main() {
            
                vec4 color = texture2D(texture, texCoord);
                vec4 t = texture2D(texture, texCoord);

                t = applyBrightness(t, brightness);
                t = applySaturation(t, saturation);
                t = applyWarmth(t, warmth);
                t = applyTint(t, tint);
                t = applyGamma(t, gamma);
                t = applyVibrant(t, vibrant);
                t = applyColorMatrix(t, colorMatrix, colorMatrixIntensity);
                t = applySepia(t, sepia);
                t = applyContrast(t, contrast);
                t = tone_curve(curveTexture, t);

                gl_FragColor = t * intensity + color * (1.0 - intensity);
            }
            
            
        """
    }
}