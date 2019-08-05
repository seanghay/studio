package com.seanghay.studio.gles.shader.filter

import android.graphics.Point
import android.graphics.PointF
import android.opengl.GLES20.GL_TEXTURE2
import com.seanghay.studio.gles.graphics.texture.Texture2d
import com.seanghay.studio.gles.graphics.texture.Texture2dUniform
import com.seanghay.studio.gles.shader.TextureShader
import java.util.*
import kotlin.collections.ArrayList
import android.opengl.GLES20
import java.nio.ByteBuffer
import kotlin.math.*


class ToneCurveFilterShader: TextureShader(fragmentShaderSource = FRAGMENT_SHADER) {

    private val defaultCurvePoints = arrayOf(
        PointF(0.0f, 0.0f),
        PointF(0.5f, 0.5f),
        PointF(1.0f, 1.0f)
    )


    var texture2d: Texture2d = Texture2d()
    var toneCurveTexture: Texture2dUniform = Texture2dUniform("toneCurveTexture", texture2d).autoInit()

    var rgbCompositeControlPoints = defaultCurvePoints
        protected set

    var redControlPoints = defaultCurvePoints
        protected set

    var greenControlPoints = defaultCurvePoints
        protected set

    var blueControlPoints = defaultCurvePoints
        protected set


    var rgbCompositeCurve: ArrayList<Float> = arrayListOf()
        protected set

    var redCurve: ArrayList<Float> = arrayListOf()
        protected set

    var greenCurve: ArrayList<Float> = arrayListOf()
        protected set

    var blueCurve: ArrayList<Float> = arrayListOf()
        protected set


    override fun beforeDrawVertices() {
        invalidateToneCurve()
        toneCurveTexture.setTextureToSlot(GL_TEXTURE2)
    }


    fun setBlueKnots(points: Array<PointF>) {
        blueControlPoints = points
        blueCurve = createSplineCurve(blueControlPoints)!! // Force to crash if nulls
    }


    fun setRedKnots(points: Array<PointF>) {
        redControlPoints = points
        redCurve = createSplineCurve(redControlPoints)!! // Force to crash if nulls
    }

    fun setGreenKnots(points: Array<PointF>) {
        greenControlPoints = points
        greenCurve = createSplineCurve(greenControlPoints)!! // Force to crash if nulls
    }

    fun setRgbKnots(points: Array<PointF>) {
        rgbCompositeControlPoints = points
        rgbCompositeCurve = createSplineCurve(rgbCompositeControlPoints)!! // Force to crash if nulls
    }


    private fun invalidateToneCurve() {
        toneCurveTexture.enable()
        if (redCurve.size >= 256 && greenCurve.size >= 256 && blueCurve.size >= 256 && rgbCompositeCurve.size >= 256) {
            val toneCurveByteArray = ByteArray(256 * 4)
            for (currentCurveIndex in 0..255) {
                // BGRA for upload to texture
                toneCurveByteArray[currentCurveIndex * 4 + 2] = (
                        min(max(currentCurveIndex + blueCurve[currentCurveIndex] + rgbCompositeCurve[currentCurveIndex], 0f), 255f).toInt() and 0xff).toByte()
                toneCurveByteArray[currentCurveIndex * 4 + 1] = (min(max(currentCurveIndex + greenCurve[currentCurveIndex] + rgbCompositeCurve[currentCurveIndex], 0f), 255f).toInt() and 0xff).toByte()
                toneCurveByteArray[currentCurveIndex * 4] = (min(max(currentCurveIndex + redCurve[currentCurveIndex] + rgbCompositeCurve[currentCurveIndex], 0f), 255f).toInt() and 0xff).toByte()
                toneCurveByteArray[currentCurveIndex * 4 + 3] = 0xff.toByte()
            }

            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                256 /*width*/,
                1 /*height*/,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                ByteBuffer.wrap(toneCurveByteArray)
            )
        }
    }

    companion object {
        // language=glsl
        const val FRAGMENT_SHADER = """
            precision mediump float;
            
            uniform sampler2D texture;
            uniform sampler2D toneCurveTexture;

            varying vec2 texCoord;
            
            void main() {
                 lowp vec4 textureColor = texture2D(texture, texCoord);
                 
                 lowp float redCurveValue = texture2D(toneCurveTexture, vec2(textureColor.r, 0.0)).r;
                 lowp float greenCurveValue = texture2D(toneCurveTexture, vec2(textureColor.g, 0.0)).g;
                 lowp float blueCurveValue = texture2D(toneCurveTexture, vec2(textureColor.b, 0.0)).b;
                 
                 gl_FragColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, textureColor.a);
            }
            
        """


        private fun createSplineCurve(points: Array<PointF>?): ArrayList<Float>? {
            if (points == null || points.isEmpty()) {
                return null
            }

            // Sort the array
            val pointsSorted = points.clone()
            Arrays.sort(pointsSorted) { point1, point2 ->
                when {
                    point1.x < point2.x -> -1
                    point1.x > point2.x -> 1
                    else -> 0
                }
            }

            // Convert from (0, 1) to (0, 255).
            val convertedPoints = arrayOfNulls<Point>(pointsSorted.size)

            for (i in points.indices) {
                val point = pointsSorted[i]
                convertedPoints[i] = Point((point.x * 255).toInt(), (point.y * 255).toInt())
            }

            val splinePoints = createSplineCurve2(convertedPoints.filterNotNull().toTypedArray()) ?: return null

            // If we have a first point like (0.3, 0) we'll be missing some points at the beginning
            // that should be 0.
            val firstSplinePoint = splinePoints.get(0)
            if (firstSplinePoint.x > 0) {
                for (i in firstSplinePoint.x downTo 0) {
                    splinePoints.add(0, Point(i, 0))
                }
            }

            // Insert points similarly at the end, if necessary.
            val lastSplinePoint = splinePoints.get(splinePoints.size - 1)
            if (lastSplinePoint.x < 255) {
                for (i in lastSplinePoint.x + 1..255) {
                    splinePoints.add(Point(i, 255))
                }
            }

            // Prepare the spline points.
            val preparedSplinePoints = ArrayList<Float>(splinePoints.size)
            for (newPoint in splinePoints) {
                val origPoint = Point(newPoint.x, newPoint.x)

                var distance = sqrt(
                    (origPoint.x - newPoint.x).toDouble().pow(2.0) + (origPoint.y - newPoint.y).toDouble().pow(2.0)
                ).toFloat()

                if (origPoint.y > newPoint.y) {
                    distance = -distance
                }

                preparedSplinePoints.add(distance)
            }

            return preparedSplinePoints
        }


        private fun createSplineCurve2(points: Array<Point>): ArrayList<Point>? {

            val sdA = createSecondDerivative(points) ?: return null

            // Is [points count] equal to [sdA count]?
            //    int n = [points count];

            val n = sdA.size

            if (n < 1) {
                return null
            }

            val sd = DoubleArray(n)

            // From NSMutableArray to sd[n];
            for (i in 0 until n) {
                sd[i] = sdA.get(i)
            }

            val output = ArrayList<Point>(n + 1)

            for (i in 0 until n - 1) {
                val cur = points[i]
                val next = points[i + 1]

                for (x in cur.x until next.x) {
                    val t = (x - cur.x).toDouble() / (next.x - cur.x)

                    val a = 1 - t
                    val h = (next.x - cur.x).toDouble()

                    var y = a * cur.y + t * next.y + h * h / 6 * ((a * a * a - a) * sd[i] + (t * t * t - t) * sd[i + 1])

                    if (y > 255.0) {
                        y = 255.0
                    } else if (y < 0.0) {
                        y = 0.0
                    }

                    output.add(Point(x, y.roundToInt()))
                }
            }

            // If the last point is (255, 255) it doesn't get added.
            if (output.size == 255) {
                output.add(points[points.size - 1])
            }
            return output
        }

        private fun createSecondDerivative(points: Array<Point>): ArrayList<Double>? {

            val n = points.size
            if (n <= 1) {
                return null
            }

            val matrix = Array(n) { DoubleArray(3) }
            val result = DoubleArray(n)
            matrix[0][1] = 1.0
            // What about matrix[0][1] and matrix[0][0]? Assuming 0 for now (Brad L.)
            matrix[0][0] = 0.0
            matrix[0][2] = 0.0

            for (i in 1 until n - 1) {
                val P1 = points[i - 1]
                val P2 = points[i]
                val P3 = points[i + 1]

                matrix[i][0] = (P2.x - P1.x).toDouble() / 6
                matrix[i][1] = (P3.x - P1.x).toDouble() / 3
                matrix[i][2] = (P3.x - P2.x).toDouble() / 6
                result[i] = (P3.y - P2.y).toDouble() / (P3.x - P2.x) - (P2.y - P1.y).toDouble() / (P2.x - P1.x)
            }

            // What about result[0] and result[n-1]? Assuming 0 for now (Brad L.)
            result[0] = 0.0
            result[n - 1] = 0.0

            matrix[n - 1][1] = 1.0
            // What about matrix[n-1][0] and matrix[n-1][2]? For now, assuming they are 0 (Brad L.)
            matrix[n - 1][0] = 0.0
            matrix[n - 1][2] = 0.0

            // solving pass1 (up->down)
            for (i in 1 until n) {
                val k = matrix[i][0] / matrix[i - 1][1]
                matrix[i][1] -= k * matrix[i - 1][2]
                matrix[i][0] = 0.0
                result[i] -= k * result[i - 1]
            }
            // solving pass2 (down->up)
            for (i in n - 2 downTo 0) {
                val k = matrix[i][2] / matrix[i + 1][1]
                matrix[i][1] -= k * matrix[i + 1][0]
                matrix[i][2] = 0.0
                result[i] -= k * result[i + 1]
            }

            val output = ArrayList<Double>(n)
            for (i in 0 until n) output.add(result[i] / matrix[i][1])

            return output
        }
    }
}