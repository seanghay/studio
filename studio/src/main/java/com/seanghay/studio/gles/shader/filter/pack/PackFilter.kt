package com.seanghay.studio.gles.shader.filter.pack

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.FloatRange
import com.seanghay.studio.gles.shader.filter.tonecurve.ToneCurve

data class PackFilter(
    @FloatRange(from = 0.0, to = 1.0)
    var intensity: Float = 1f,

    @FloatRange(from = -1.0, to = 1.0)
    var brightness: Float = 0f,

    // That might not be true
    @FloatRange(from = 0.0, to = 2.0)
    var contrast: Float = 1f,

    @FloatRange(from = 0.0, to = 2.0)
    var saturation: Float = 1f,

    @FloatRange(from = -0.5, to = 0.5)
    var warmth: Float = 0f,

    @FloatRange(from = 0.0, to = 1.0)
    var tint: Float = 0f,

    @FloatRange(from = 0.0, to = 2.0)
    var gamma: Float = 1f,

    @FloatRange(from = 0.0, to = 1.0)
    var vibrant: Float = 0f,

    @FloatRange(from = 0.0, to = 1.0)
    var sepia: Float = 0f,

    var toneCurve: ToneCurve? = null

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readParcelable(ToneCurve::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(intensity)
        parcel.writeFloat(brightness)
        parcel.writeFloat(contrast)
        parcel.writeFloat(saturation)
        parcel.writeFloat(warmth)
        parcel.writeFloat(tint)
        parcel.writeFloat(gamma)
        parcel.writeFloat(vibrant)
        parcel.writeFloat(sepia)
        parcel.writeParcelable(toneCurve, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PackFilter> {
        override fun createFromParcel(parcel: Parcel): PackFilter {
            return PackFilter(parcel)
        }

        override fun newArray(size: Int): Array<PackFilter?> {
            return arrayOfNulls(size)
        }
    }
}
