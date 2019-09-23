package com.seanghay.studio.gles.shader.filter.tonecurve.simple

import android.graphics.PointF
import com.seanghay.studio.gles.shader.filter.tonecurve.ToneCurve


object SimpleToneCurve {

    fun getAweStruckVibe(): ToneCurve {
        val rgb = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(80f / 255f, 43f / 255f),
            PointF(149f / 255f, 102f / 255f),
            PointF(201f / 255f, 173f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        val red = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(125f / 255f, 147f / 255f),
            PointF(177f / 255f, 199f / 255f),
            PointF(213f / 255f, 228f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )


        val green = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(57f / 255f, 76f / 255f),
            PointF(103f / 255f, 130f / 255f),
            PointF(167f / 255f, 192f / 255f),
            PointF(211f / 255f, 229f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )


        val blue = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(38f / 255f, 62f / 255f),
            PointF(75f / 255f, 112f / 255f),
            PointF(116f / 255f, 158f / 255f),
            PointF(171f / 255f, 204f / 255f),
            PointF(212f / 255f, 233f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        return ToneCurve(rgb = rgb, r = red, g = green, b = blue)
    }


    fun getBlueMess(): ToneCurve {
        val red = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(86f / 255f, 34f / 255f),
            PointF(117f / 255f, 41f / 255f),
            PointF(146f / 255f, 80f / 255f),
            PointF(170f / 255f, 151f / 255f),
            PointF(200f / 255f, 214f / 255f),
            PointF(225f / 255f, 242f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        return ToneCurve(r = red)
    }

    fun getStarLit(): ToneCurve {
        val rgb = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(34f / 255f, 6f / 255f),
            PointF(69f / 255f, 23f / 255f),
            PointF(100f / 255f, 58f / 255f),
            PointF(150f / 255f, 154f / 255f),
            PointF(176f / 255f, 196f / 255f),
            PointF(207f / 255f, 233f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        return ToneCurve(rgb)
    }

    fun getLimeStutter(): ToneCurve {
        val blue = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(165f / 255f, 114f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        return ToneCurve(b = blue)
    }

    fun getNightWhisper(): ToneCurve {

        val rgb = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(174f / 255f, 109f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        val red = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(70f / 255f, 114f / 255f),
            PointF(157f / 255f, 145f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )


        val green = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(109f / 255f, 138f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        val blue = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(113f / 255f, 152f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        return ToneCurve(rgb, red, green, blue)
    }

    fun getAmazon(): ToneCurve {
        val blue = arrayOf(
            PointF(0f / 255f, 0f / 255f),
            PointF(11f / 255f, 40f / 255f),
            PointF(36f / 255f, 99f / 255f),
            PointF(86f / 255f, 151f / 255f),
            PointF(167f / 255f, 209f / 255f),
            PointF(255f / 255f, 255f / 255f)
        )

        return ToneCurve(b = blue)
    }

}
