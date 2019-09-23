package com.seanghay.studio.gles.shader.filter.tonecurve

import android.graphics.Point
import android.graphics.PointF
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

interface ToneCurveUtils {

    fun createSplineCurve(points: Array<PointF>?): ArrayList<Float>? {
        if (points == null) return null
        if (points.isEmpty()) return null

        val pointsSorted = points.clone()
        Arrays.sort(pointsSorted) { point1, point2 ->
            when {
                point1.x < point2.x -> -1
                point1.x > point2.x -> 1
                else -> 0
            }
        }

        val convertedPoints = arrayOfNulls<Point>(pointsSorted.size)
        for (i in points.indices) {
            val point = pointsSorted[i]
            convertedPoints[i] =
                Point((point.x * 255f).toInt(), (point.y * 255f).toInt())
        }

        val convertedPointsNotNull = convertedPoints.filterNotNull().toTypedArray()
        val splinePoints = createSplineCurve2(convertedPointsNotNull) ?: return null

        val firstSplinePoint = splinePoints[0]
        if (firstSplinePoint.x > 0) {
            for (i in firstSplinePoint.x downTo 0) {
                splinePoints.add(0, Point(i, 0))
            }
        }

        val lastSplinePoint = splinePoints[splinePoints.size - 1]

        if (lastSplinePoint.x < 255) {
            for (i in lastSplinePoint.x + 1..255) {
                splinePoints.add(Point(i, 255))
            }
        }

        val preparedSplinePoints = ArrayList<Float>(splinePoints.size)
        for (newPoint in splinePoints) {
            val origPoint = Point(newPoint.x, newPoint.x)

            var distance = sqrt(
                (origPoint.x - newPoint.x).toDouble().pow(2.0) + (origPoint.y - newPoint.y).toDouble().pow(
                    2.0
                )
            ).toFloat()

            if (origPoint.y > newPoint.y) {
                distance = -distance
            }

            preparedSplinePoints.add(distance)
        }

        return preparedSplinePoints
    }


    fun createSplineCurve2(points: Array<Point>): ArrayList<Point>? {
        val sdA = createSecondDerivative(points) ?: return null
        val n = sdA.size
        if (n < 1) return null

        val sd = DoubleArray(n)
        for (i in 0 until n) {
            sd[i] = sdA[i]
        }

        val output = ArrayList<Point>(n + 1)

        for (i in 0 until n - 1) {
            val cur = points[i]
            val next = points[i + 1]

            for (x in cur.x until next.x) {

                val t = (x - cur.x).toDouble() / (next.x - cur.x)
                val a = 1 - t
                val h = (next.x - cur.x).toDouble()

                var y =
                    a * cur.y + t * next.y + h * h / 6 * ((a * a * a - a) * sd[i] + (t * t * t - t) * sd[i + 1])

                if (y > 255.0) {
                    y = 255.0
                } else if (y < 0.0) {
                    y = 0.0
                }

                output.add(Point(x, y.roundToInt()))
            }
        }

        if (output.size == 255) output.add(points[points.size - 1])
        return output
    }



    fun createSecondDerivative(points: Array<Point>): ArrayList<Double>? {
        val n = points.size
        if (n <= 1) return null

        val matrix = Array(n) { DoubleArray(3) }
        val result = DoubleArray(n)

        matrix[0][1] = 1.0
        matrix[0][0] = 0.0
        matrix[0][2] = 0.0

        for (i in 1 until n - 1) {
            val p1 = points[i - 1]
            val p2 = points[i]
            val p3 = points[i + 1]

            matrix[i][0] = (p2.x - p1.x).toDouble() / 6
            matrix[i][1] = (p3.x - p1.x).toDouble() / 3
            matrix[i][2] = (p3.x - p2.x).toDouble() / 6
            result[i] =
                (p3.y - p2.y).toDouble() / (p3.x - p2.x) - (p2.y - p1.y).toDouble() / (p2.x - p1.x)
        }


        result[0] = 0.0
        result[n - 1] = 0.0

        matrix[n - 1][1] = 1.0
        matrix[n - 1][0] = 0.0
        matrix[n - 1][2] = 0.0

        for (i in 1 until n) {
            val k = matrix[i][0] / matrix[i - 1][1]
            matrix[i][1] -= k * matrix[i - 1][2]
            matrix[i][0] = 0.0
            result[i] -= k * result[i - 1]
        }

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