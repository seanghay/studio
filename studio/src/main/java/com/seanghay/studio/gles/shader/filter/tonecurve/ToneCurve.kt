package com.seanghay.studio.gles.shader.filter.tonecurve

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import java.io.IOException
import java.io.InputStream

data class ToneCurve(
    val rgb: Array<PointF>? = null,
    val r: Array<PointF>? = null,
    val g: Array<PointF>? = null,
    val b: Array<PointF>? = null
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.createTypedArray(PointF.CREATOR),
        parcel.createTypedArray(PointF.CREATOR),
        parcel.createTypedArray(PointF.CREATOR),
        parcel.createTypedArray(PointF.CREATOR)
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToneCurve
        if (rgb != null) {
            if (other.rgb == null) return false
            if (!rgb.contentEquals(other.rgb)) return false
        } else if (other.rgb != null) return false
        if (r != null) {
            if (other.r == null) return false
            if (!r.contentEquals(other.r)) return false
        } else if (other.r != null) return false
        if (g != null) {
            if (other.g == null) return false
            if (!g.contentEquals(other.g)) return false
        } else if (other.g != null) return false
        if (b != null) {
            if (other.b == null) return false
            if (!b.contentEquals(other.b)) return false
        } else if (other.b != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rgb?.contentHashCode() ?: 0
        result = 31 * result + (r?.contentHashCode() ?: 0)
        result = 31 * result + (g?.contentHashCode() ?: 0)
        result = 31 * result + (b?.contentHashCode() ?: 0)
        return result
    }

    companion object {
        @JvmField
        val CREATOR  = object: Parcelable.Creator<ToneCurve> {
            override fun createFromParcel(parcel: Parcel): ToneCurve {
                return ToneCurve(parcel)
            }

            override fun newArray(size: Int): Array<ToneCurve?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun getDefault(): ToneCurve {
            val default = arrayOf(PointF(0.0f, 0.0f), PointF(0.5f, 0.5f), PointF(1.0f, 1.0f))
            return ToneCurve(
                default, default, default, default
            )
        }


        @JvmStatic
        fun fromInputStream(input: InputStream): ToneCurve {
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

            val rgb = curves.getOrNull(0)?.takeIf { it.isNotEmpty() }
            val r = curves.getOrNull(1)?.takeIf { it.isNotEmpty() }
            val g = curves.getOrNull(2)?.takeIf { it.isNotEmpty() }
            val b = curves.getOrNull(3)?.takeIf { it.isNotEmpty() }
            return ToneCurve(rgb, r, g, b)
        }

        @Throws(IOException::class)
        private fun InputStream.readShort(): Short {
            return (read() shl 8 or read()).toShort()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedArray(rgb, flags)
        parcel.writeTypedArray(r, flags)
        parcel.writeTypedArray(g, flags)
        parcel.writeTypedArray(b, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

}