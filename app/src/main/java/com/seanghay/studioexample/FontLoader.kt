package com.seanghay.studioexample

import android.content.res.AssetManager
import android.graphics.Typeface
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
    ) {

        private var typeface: Typeface? = null

        fun getTypeface(assetManager: AssetManager): Typeface {
            return typeface ?: Typeface.createFromAsset(assetManager, path).also {
                this.typeface = it
            }
        }

        override fun toString(): String {
            return name
        }
    }
}