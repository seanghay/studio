package com.seanghay.studioexample

import android.content.res.AssetManager
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import java.io.IOException

class FontLoader(private val assetManager: AssetManager) {

    private val fonts: ArrayList<FontFamily> = arrayListOf()

    fun getFonts(): List<FontFamily> {
        if (fonts.isEmpty())
        walk("fonts", "fonts")
        return fonts
    }


    private fun walk(dir: String, path: String): Boolean {

        try {
            val list = assetManager.list(path) ?: return true
            if (list.isNotEmpty()) {
                // This is a folder
                for (file in list) {
                    if (!walk(file, "$path/$file"))
                        return false
                    else {
                        if (file.endsWith(".ttf"))
                        fonts.add(FontFamily(file.replace(".ttf", ""), dir, "$path/$file"))
                    }
                }
            }
        } catch (e: IOException) {
            return false
        }
        return true
    }


    data class FontFamily(
        val name: String,
        val family: String,
        val path: String
    ): Parcelable {

        @Volatile
        private var typeface: Typeface? = null

        constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readString()!!
        )


        fun getTypeface(assetManager: AssetManager): Typeface {
            return typeface ?: Typeface.createFromAsset(assetManager, path).also {
                this.typeface = it
            }
        }

        override fun toString(): String {
            return name
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeString(family)
            parcel.writeString(path)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<FontFamily> {
            override fun createFromParcel(parcel: Parcel): FontFamily {
                return FontFamily(parcel)
            }

            override fun newArray(size: Int): Array<FontFamily?> {
                return arrayOfNulls(size)
            }
        }
    }
}