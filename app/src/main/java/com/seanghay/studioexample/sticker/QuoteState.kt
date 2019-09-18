package com.seanghay.studioexample.sticker

import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import com.seanghay.studioexample.FontLoader

data class QuoteState(
    var text: CharSequence = "",
    var textSize: Float,
    var textColor: Int = Color.BLACK,
    var fontFamily: FontLoader.FontFamily?,
    var position: PointF = PointF(0f, 0f),
    var scaleFactor: Float = 1f,
    var textRotationAngle: Float = 0f
): Parcelable {

    private constructor(parcel: Parcel) : this(
        parcel.readString() ?: "" ,
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readParcelable(FontLoader.FontFamily::class.java.classLoader),
        parcel.readParcelable(PointF::class.java.classLoader)!!,
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text.toString())
        parcel.writeFloat(textSize)
        parcel.writeInt(textColor)
        parcel.writeParcelable(fontFamily, flags)
        parcel.writeParcelable(position, flags)
        parcel.writeFloat(scaleFactor)
        parcel.writeFloat(textRotationAngle)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun applyTo(assetManager: AssetManager, quoteDesigner: QuoteDesigner) {
        with(quoteDesigner) {
            setText(text)
            setTextSize(textSize)
            setTextColor(textColor)
            fontFamily?.getTypeface(assetManager)?.let { setTypeface(it) }
            setPosition(position)
            setScale(scaleFactor)
            setTextRotation(textRotationAngle)
        }
    }

    companion object {
        fun from(quoteDesigner: QuoteDesigner, fontFamily: FontLoader.FontFamily?): QuoteState {
            return quoteDesigner.run {
                QuoteState(getText(),
                    getTextSize(),
                    getTextColor(),
                    fontFamily,
                    getPosition(),
                    getScale(),
                    getTextRotation())
            }
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<QuoteState> {
            override fun createFromParcel(parcel: Parcel): QuoteState {
                return QuoteState(parcel)
            }

            override fun newArray(size: Int): Array<QuoteState?> {
                return arrayOfNulls(size)
            }
        }
    }


}