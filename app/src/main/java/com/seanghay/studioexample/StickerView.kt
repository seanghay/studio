package com.seanghay.studioexample

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.Px

class StickerView : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    )

    fun setText(text: CharSequence) {

    }

    fun setTextSize(@Px size: Int) {

    }

    fun setTypeface(fontFamily: FontLoader.FontFamily) {

    }

    fun setTextColor(@ColorInt color: Int) {

    }

}