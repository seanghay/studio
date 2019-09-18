package com.seanghay.studioexample.sticker

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.Px

interface QuoteDesigner {

    fun getText(): CharSequence
    @Px
    fun getTextSize(): Float
    fun getTypeface(): Typeface

    @ColorInt
    fun getTextColor(): Int
    fun getTextRotation(): Float
    fun getScale(): Float
    fun getPosition(): PointF

    fun setText(text: CharSequence)
    fun setTextSize(@Px size: Float)
    fun setTextColor(@ColorInt textColor: Int)
    fun setTypeface(typeface: Typeface)
    fun getBitmap(): Bitmap
    fun updateQuote()
    fun setPosition(position: PointF)
    fun setTextRotation(angle: Float)
    fun setScale(scaleFactor: Float)
}