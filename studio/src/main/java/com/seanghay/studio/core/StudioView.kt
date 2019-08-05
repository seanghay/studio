package com.seanghay.studio.core

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

class StudioView: TextureView {

    private val studioEngine = StudioEngine()

    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int): super(context, attributeSet, defStyle)

    init { surfaceTextureListener = studioEngine }
}