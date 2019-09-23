package com.seanghay.studio.gles.shader.filter.tonecurve

import android.graphics.PointF

object SimpleToneCurvePack {

    @JvmField
    val starLitRgbKnots = arrayOf(
        PointF(0.0f, 0.0f),
        PointF(34.0f, 6.0f),
        PointF(69.0f, 23.0f),
        PointF(100.0f, 58.0f),
        PointF(150.0f, 154.0f),
        PointF(176.0f, 196.0f),
        PointF(207.0f, 233.0f),
        PointF(255.0f, 255.0f)
    ).transforms()

    @JvmField
    val blueMessRedKnots = arrayOf(
        PointF(0.0f, 0.0f),
        PointF(86.0f, 34.0f),
        PointF(117.0f, 41.0f),
        PointF(146.0f, 80.0f),
        PointF(170.0f, 151.0f),
        PointF(200.0f, 214.0f),
        PointF(225.0f, 242.0f),
        PointF(255.0f, 255.0f)
    ).transforms()

    fun Array<PointF>.transforms(): Array<PointF> {
        return map { PointF(it.x / 255f, it.y / 255f) }.toTypedArray()
    }
}